#!/usr/bin/env bash
# ==============================================================================
# Assessify - End-to-End Live Demo & Seed Script
# Simulates full tenant onboarding, quiz authoring, candidate sitting, and grading.
# ==============================================================================

set -e

BASE_URL="http://localhost:8080"
echo "🚀 Starting Assessify Demo Run..."

# 1. Healthcheck API Gateway
echo -e "\n1. Checking API Gateway Health..."
curl -s "${BASE_URL}/actuator/health" || { echo "❌ API Gateway not reachable at ${BASE_URL}"; exit 1; }

# 2. Register Organization Admin
echo -e "\n\n2. Registering Institute Admin..."
ADMIN_REG=$(curl -s -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dean@stanford.edu",
    "password": "Password123!",
    "name": "Dr. Sarah Dean",
    "orgName": "Stanford University",
    "orgSlug": "stanford-edu"
  }')
echo "Response: ${ADMIN_REG}"

# 3. Authenticate & Obtain RS256 JWT
echo -e "\n\n3. Authenticating Admin..."
LOGIN_RES=$(curl -s -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "dean@stanford.edu",
    "password": "Password123!"
  }')
echo "Login Token Response Received."
TOKEN=$(echo "${LOGIN_RES}" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# 4. Create an Exam
echo -e "\n\n4. Creating Computer Science Midterm Exam..."
QUIZ_RES=$(curl -s -X POST "${BASE_URL}/api/quizzes" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "CS106B: Data Structures & Algorithms",
    "description": "Comprehensive midterm covering trees, heaps, and graph search algorithms."
  }')
echo "Quiz Created: ${QUIZ_RES}"
QUIZ_ID=$(echo "${QUIZ_RES}" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

# 5. Author Questions into Question Bank (Answer key stored securely)
echo -e "\n\n5. Authoring Exam Questions..."
Q1_RES=$(curl -s -X POST "${BASE_URL}/api/questions" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"quizId\": \"${QUIZ_ID}\",
    \"text\": \"What is the worst-case time complexity of searching in a Balanced Binary Search Tree (AVL Tree)?\",
    \"difficulty\": \"EASY\",
    \"category\": \"Data Structures\",
    \"marks\": 2,
    \"negativeMarks\": 0.5,
    \"options\": [
      {\"text\": \"O(log n)\", \"correct\": true},
      {\"text\": \"O(n)\", \"correct\": false},
      {\"text\": \"O(n log n)\", \"correct\": false},
      {\"text\": \"O(1)\", \"correct\": false}
    ]
  }")
echo "Question 1 Created: ${Q1_RES}"

# 6. Candidate Invitation
echo -e "\n\n6. Bulk Inviting Candidates..."
INVITE_RES=$(curl -s -X POST "${BASE_URL}/api/quizzes/${QUIZ_ID}/invitations/bulk" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "emails": ["alice@student.stanford.edu", "bob@student.stanford.edu"]
  }')
echo "Invitations Issued: ${INVITE_RES}"

# 7. Candidate Starts Exam Attempt
echo -e "\n\n7. Candidate (Alice) Starts Exam Attempt..."
ATTEMPT_RES=$(curl -s -X POST "${BASE_URL}/api/attempts/start" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"quizId\": \"${QUIZ_ID}\",
    \"durationMinutes\": 45
  }")
echo "Attempt Started: ${ATTEMPT_RES}"
ATTEMPT_ID=$(echo "${ATTEMPT_RES}" | grep -o '"attemptId":"[^"]*' | cut -d'"' -f4)

# 8. Fetch Candidate Deterministic Paper
echo -e "\n\n8. Fetching Deterministic Paper (Candidate view - Answer keys omitted)..."
PAPER_RES=$(curl -s -X GET "${BASE_URL}/api/questions/quiz/${QUIZ_ID}/paper?attemptId=${ATTEMPT_ID}" \
  -H "Authorization: Bearer ${TOKEN}")
echo "Candidate Paper: ${PAPER_RES}"

# 9. Proctoring Event Ingestion (Monotonic Stream)
echo -e "\n\n9. Streaming Proctoring Telemetry (Heartbeat)..."
PROCTOR_RES=$(curl -s -X POST "${BASE_URL}/api/attempts/${ATTEMPT_ID}/proctor-events" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "HEARTBEAT",
    "sequenceNumber": 1,
    "metadata": "{\"tabActive\": true, \"fullScreen\": true}"
  }')
echo "Proctor Status: ${PROCTOR_RES}"

# 10. Submit Attempt
echo -e "\n\n10. Submitting Exam Attempt..."
SUBMIT_RES=$(curl -s -X POST "${BASE_URL}/api/attempts/${ATTEMPT_ID}/submit" \
  -H "Authorization: Bearer ${TOKEN}")
echo "Exam Submission: ${SUBMIT_RES}"

# 11. View Cohort Analytics & Leaderboard
echo -e "\n\n11. Fetching Exam Leaderboard..."
LEADERBOARD=$(curl -s -X GET "${BASE_URL}/api/results/quiz/${QUIZ_ID}/leaderboard" \
  -H "Authorization: Bearer ${TOKEN}")
echo "Leaderboard: ${LEADERBOARD}"

echo -e "\n\n12. Fetching Psychometric Item Analysis..."
ITEM_ANALYSIS=$(curl -s -X GET "${BASE_URL}/api/results/quiz/${QUIZ_ID}/item-analysis" \
  -H "Authorization: Bearer ${TOKEN}")
echo "Item Analysis: ${ITEM_ANALYSIS}"

echo -e "\n🎉 Assessify End-to-End Demo Complete!"
