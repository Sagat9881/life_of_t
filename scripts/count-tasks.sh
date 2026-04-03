#!/usr/bin/env bash
# §3.1 — Count tasks by status using directory-based layout
# Layout: tasks/{category}/{status}/*.md
# Status dirs: done | in_progress
# Categories: backend | narrative | frontend | assets | analytics
set -euo pipefail

TASKS_DIR="${1:-tasks}"

zero_output() {
  echo "TASKS_IN_PROGRESS=0"
  echo "TASKS_DONE=0"
  echo "TASKS_TOTAL=0"
  echo "TASKS_BACKEND=0"
  echo "TASKS_NARRATIVE=0"
  echo "TASKS_FRONTEND=0"
  echo "TASKS_ASSETS=0"
  echo "TASKS_ANALYTICS=0"
}

if [ ! -d "$TASKS_DIR" ]; then
  zero_output
  exit 0
fi

count_md_in() {
  local dir="$1"
  if [ ! -d "$dir" ]; then
    echo 0
    return
  fi
  find "$dir" -maxdepth 1 -name '*.md' -not -name '.gitkeep' 2>/dev/null | wc -l | tr -d ' '
}

# ─── Status counts (directory-based) ───────────────────────────────────────
IN_PROGRESS=0
DONE=0

while IFS= read -r -d '' dir; do
  dirname_base=$(basename "$dir")
  cnt=$(count_md_in "$dir")
  case "$dirname_base" in
    in_progress) IN_PROGRESS=$((IN_PROGRESS + cnt)) ;;
    done)        DONE=$((DONE + cnt)) ;;
  esac
done < <(find "$TASKS_DIR" -mindepth 2 -maxdepth 2 -type d \( -name 'in_progress' -o -name 'done' \) -print0 2>/dev/null)

TOTAL=$((IN_PROGRESS + DONE))

# ─── Per-category counts ────────────────────────────────────────────────────
cat_count() {
  local cat_dir="${TASKS_DIR}/$1"
  if [ ! -d "$cat_dir" ]; then
    echo 0
    return
  fi
  find "$cat_dir" -name '*.md' -not -name '.gitkeep' 2>/dev/null | wc -l | tr -d ' '
}

BACKEND=$(cat_count backend)
NARRATIVE=$(cat_count narrative)
FRONTEND=$(cat_count frontend)
ASSETS=$(cat_count assets)
ANALYTICS=$(cat_count analytics)

echo "TASKS_IN_PROGRESS=${IN_PROGRESS}"
echo "TASKS_DONE=${DONE}"
echo "TASKS_TOTAL=${TOTAL}"
echo "TASKS_BACKEND=${BACKEND}"
echo "TASKS_NARRATIVE=${NARRATIVE}"
echo "TASKS_FRONTEND=${FRONTEND}"
echo "TASKS_ASSETS=${ASSETS}"
echo "TASKS_ANALYTICS=${ANALYTICS}"
