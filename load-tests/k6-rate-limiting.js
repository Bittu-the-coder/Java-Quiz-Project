import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

export const throttled429Counter = new Counter('throttled_429_requests');
export const allowed200Counter = new Counter('allowed_requests');
export const throttleSuccessRate = new Rate('throttle_success_rate');

export const options = {
  scenarios: {
    brute_force_attack: {
      executor: 'constant-vus',
      vus: 10,
      duration: '30s',
    },
  },
  thresholds: {
    'throttle_success_rate': ['rate>0.80'], // >80% of aggressive requests should be properly throttled
  },
};

const BASE_URL = __ENV.GATEWAY_URL || 'http://localhost:8080';

export default function () {
  const loginPayload = JSON.stringify({
    email: 'attacker@target.com',
    password: 'wrongpassword',
  });

  const headers = {
    'Content-Type': 'application/json',
    'X-Forwarded-For': '203.0.113.195', // Single attacker IP
  };

  const res = http.post(`${BASE_URL}/api/auth/login`, loginPayload, { headers });

  if (res.status === 429) {
    throttled429Counter.add(1);
    throttleSuccessRate.add(1);
    check(res, {
      'has Retry-After header': (r) => r.headers['Retry-After'] !== undefined,
      'has X-RateLimit-Limit': (r) => r.headers['X-RateLimit-Limit'] !== undefined,
    });
  } else {
    allowed200Counter.add(1);
    throttleSuccessRate.add(0);
  }
}
