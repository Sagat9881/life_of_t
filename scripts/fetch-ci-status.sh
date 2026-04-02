#!/usr/bin/env bash
# §3.4 — Fetch GitHub API check-runs for HEAD commit
# Returns CI_STATUS=unknown on any network/API error
set -uo pipefail

REPO_OWNER="${REPO_OWNER:-Sagat9881}"
REPO_NAME="${REPO_NAME:-life_of_t}"
GH_TOKEN="${GH_TOKEN:-${GITHUB_TOKEN:-}}"
REF="${CI_REF:-${GITHUB_SHA:-HEAD}}"

if [ -z "$GH_TOKEN" ]; then
  echo "CI_STATUS=unknown"
  echo "CI_CONCLUSION=unknown"
  echo "CI_TOTAL_RUNS=0"
  exit 0
fi

API_URL="https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/commits/${REF}/check-runs"

RESPONSE=$(curl -sf \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${API_URL}" 2>/dev/null) || true

if [ -z "$RESPONSE" ]; then
  echo "CI_STATUS=unknown"
  echo "CI_CONCLUSION=unknown"
  echo "CI_TOTAL_RUNS=0"
  exit 0
fi

TOTAL_COUNT=$(echo "$RESPONSE" | jq -r '.total_count // 0' 2>/dev/null || echo 0)

if [ "$TOTAL_COUNT" -eq 0 ] 2>/dev/null; then
  echo "CI_STATUS=unknown"
  echo "CI_CONCLUSION=unknown"
  echo "CI_TOTAL_RUNS=0"
  exit 0
fi

# Aggregate: if any run is in_progress → in_progress; else derive from conclusions
IN_PROGRESS_COUNT=$(echo "$RESPONSE" | jq '[.check_runs[] | select(.status == "in_progress")] | length' 2>/dev/null || echo 0)
FAILURE_COUNT=$(echo "$RESPONSE" | jq '[.check_runs[] | select(.conclusion == "failure" or .conclusion == "timed_out" or .conclusion == "cancelled")] | length' 2>/dev/null || echo 0)
SUCCESS_COUNT=$(echo "$RESPONSE" | jq '[.check_runs[] | select(.conclusion == "success")] | length' 2>/dev/null || echo 0)

if [ "${IN_PROGRESS_COUNT:-0}" -gt 0 ]; then
  STATUS="in_progress"
  CONCLUSION="pending"
elif [ "${FAILURE_COUNT:-0}" -gt 0 ]; then
  STATUS="completed"
  CONCLUSION="failure"
elif [ "${SUCCESS_COUNT:-0}" -gt 0 ]; then
  STATUS="completed"
  CONCLUSION="success"
else
  STATUS="unknown"
  CONCLUSION="unknown"
fi

echo "CI_STATUS=${STATUS}"
echo "CI_CONCLUSION=${CONCLUSION}"
echo "CI_TOTAL_RUNS=${TOTAL_COUNT}"
