import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// Custom metrics for p95/p99 SLA tracking
export const autosaveLatency = new Trend('autosave_duration', true);
export const submissionLatency = new Trend('submission_duration', true);
export const errorRate = new Rate('error_rate');
export const successfulSubmissions = new Counter('successful_submissions');

export const options = {
  scenarios: {
    // 10,000 candidates exam concurrency scenario
    concurrent_candidates: {
      executor: 'ramping-vus',
      startVUs: 100,
      stages: [
        { duration: '30s', target: 2000 },  // Ramp to 2,000 VUs
        { duration: '1m', target: 5000 },   // Scale to 5,000 VUs
        { duration: '2m', target: 10000 },  // Peak at 10,000 concurrent candidates
        { duration: '1m', target: 10000 },  // Sustained exam load
        { duration: '30s', target: 0 },     // Ramp down as exam finishes
      ],
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<250', 'p(99)<500'], // 95% of requests must complete within 250ms
    'autosave_duration': ['p(95)<150', 'p(99)<300'], // Autosave p95 < 150ms
    'submission_duration': ['p(95)<300', 'p(99)<600'], // Submit p95 < 300ms
    'error_rate': ['rate<0.01'],                     // Less than 1% errors permitted
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';
const QUIZ_ID = __ENV.QUIZ_ID || '11111111-1111-1111-1111-111111111111';
const ORG_ID = __ENV.ORG_ID || '00000000-0000-0000-0000-000000000001';

export default function () {
  const vuId = __VU;
  const iteration = __ITER;
  const candidateEmail = `candidate_${vuId}_${iteration}@benchmark.assessify.io`;

  const headers = {
    'Content-Type': 'application/json',
    'X-Org-Id': ORG_ID,
    'X-User-Email': candidateEmail,
    'X-User-Role': 'STUDENT',
  };

  // 1. Start Exam Attempt
  const startPayload = JSON.stringify({
    quizId: QUIZ_ID,
    durationMinutes: 60,
  });

  const startRes = http.post(`${BASE_URL}/api/attempts/start`, startPayload, { headers });
  const startOk = check(startRes, {
    'start status is 201 or 200': (r) => r.status === 201 || r.status === 200,
  });

  if (!startOk) {
    errorRate.add(1);
    return;
  }

  const attemptData = JSON.parse(startRes.body);
  const attemptId = attemptData.attemptId;

  // 2. Fetch Deterministic Paper (Candidate view)
  const paperRes = http.get(`${BASE_URL}/api/questions/quiz/${QUIZ_ID}/paper?attemptId=${attemptId}`, { headers });
  check(paperRes, {
    'paper status is 200': (r) => r.status === 200,
  });

  // 3. Candidate Autosave Answers (5 questions simulated)
  for (let q = 1; q <= 5; q++) {
    const questionId = `22222222-2222-2222-2222-${String(q).padStart(12, '0')}`;
    const selectedOptionId = `33333333-3333-3333-3333-${String(q).padStart(12, '0')}`;

    const autosavePayload = JSON.stringify({
      questionId: questionId,
      selectedOptionId: selectedOptionId,
    });

    const autosaveStart = Date.now();
    const saveRes = http.put(`${BASE_URL}/api/attempts/${attemptId}/answer`, autosavePayload, { headers });
    autosaveLatency.add(Date.now() - autosaveStart);

    check(saveRes, {
      'autosave status is 200': (r) => r.status === 200,
    });

    // 4. Stream Proctoring Heartbeat Event
    const proctorPayload = JSON.stringify({
      eventType: 'HEARTBEAT',
      sequenceNumber: q,
      metadata: '{"tabFocused":true}',
    });

    http.post(`${BASE_URL}/api/attempts/${attemptId}/proctor-events`, proctorPayload, { headers });

    sleep(0.5); // 500ms candidate pacing between answers
  }

  // 5. Final Exam Submission
  const submitStart = Date.now();
  const submitRes = http.post(`${BASE_URL}/api/attempts/${attemptId}/submit`, null, { headers });
  submissionLatency.add(Date.now() - submitStart);

  const submitOk = check(submitRes, {
    'submit status is 200': (r) => r.status === 200,
  });

  if (submitOk) {
    successfulSubmissions.add(1);
    errorRate.add(0);
  } else {
    errorRate.add(1);
  }

  // 6. Test Idempotency: re-submit duplicate request (must return 200 cleanly without re-scoring)
  const duplicateRes = http.post(`${BASE_URL}/api/attempts/${attemptId}/submit`, null, { headers });
  check(duplicateRes, {
    'idempotent resubmit is 200': (r) => r.status === 200,
  });
}
