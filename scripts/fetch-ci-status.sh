#!/usr/bin/env bash
# §3.4 — Fetch GitHub API check-runs for a specific commit SHA
# Returns CI_STATUS=unknown on any network/API error
# Supports pagination (up to 500 check-runs via per_page=100 x 5 pages)
set -uo pipefail

REPO_OWNER="${REPO_OWNER:-Sagat9881}"
REPO_NAME="${REPO_NAME:-life_of_t}"
GH_TOKEN="${GH_TOKEN:-${GITHUB_TOKEN:-}}"
REF="${CI_REF:-${GITHUB_SHA:-}}"

# ─── Fallback helpers ────────────────────────────────────────────
ci_unknown() {
  echo "CI_STATUS=unknown"
  echo "CI_CONCLUSION=unknown"
  echo "CI_TOTAL_RUNS=0"
  echo "CI_FAILED_CHECKS=0"
  echo "CI_SUCCESS_CHECKS=0"
  exit 0
}

if [ -z "$GH_TOKEN" ]; then
  ci_unknown
fi

# Guard: HEAD literal is not a valid GitHub API ref — require a real SHA
if [ -z "$REF" ] || [ "$REF" = "HEAD" ]; then
  echo "[WARN] fetch-ci-status: CI_REF/GITHUB_SHA not set or is literal HEAD — skipping API call" >&2
  ci_unknown
fi

API_BASE="https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/commits/${REF}/check-runs"

# ─── Paginated fetch (max 5 pages = 500 check-runs) ──────────────────
ALL_RUNS='[]'
PAGE=1
MAX_PAGES=5

while [ "$PAGE" -le "$MAX_PAGES" ]; do
  RESPONSE=$(curl -sf \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "${API_BASE}?per_page=100&page=${PAGE}" 2>/dev/null) || true

  if [ -z "$RESPONSE" ]; then
    break
  fi

  PAGE_RUNS=$(echo "$RESPONSE" | jq -c '.check_runs // []' 2>/dev/null || echo '[]')
  PAGE_COUNT=$(echo "$PAGE_RUNS" | jq 'length' 2>/dev/null || echo 0)

  ALL_RUNS=$(printf '%s\n%s' "$ALL_RUNS" "$PAGE_RUNS" | jq -s '.[0] + .[1]' 2>/dev/null || echo '[]')

  # If page returned fewer than 100, we've reached the last page
  if [ "${PAGE_COUNT:-0}" -lt 100 ]; then
    break
  fi

  PAGE=$((PAGE + 1))
done

TOTAL_COUNT=$(echo "$ALL_RUNS" | jq 'length' 2>/dev/null || echo 0)

if [ "${TOTAL_COUNT:-0}" -eq 0 ]; then
  ci_unknown
fi

# ─── Aggregate status ─────────────────────────────────────────────
IN_PROGRESS_COUNT=$(echo "$ALL_RUNS" | jq '[.[] | select(.status == "in_progress")] | length' 2>/dev/null || echo 0)
FAILURE_COUNT=$(echo "$ALL_RUNS" | jq '[.[] | select(.conclusion == "failure" or .conclusion == "timed_out" or .conclusion == "cancelled")] | length' 2>/dev/null || echo 0)
SUCCESS_COUNT=$(echo "$ALL_RUNS" | jq '[.[] | select(.conclusion == "success")] | length' 2>/dev/null || echo 0)

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
echo "CI_FAILED_CHECKS=${FAILURE_COUNT}"
echo "CI_SUCCESS_CHECKS=${SUCCESS_COUNT}"
