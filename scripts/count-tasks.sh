#!/usr/bin/env bash
# §3.1 — Parse tasks/**/*.md and count by status
# Statuses: TODO | IN_PROGRESS | REVIEW | DONE | BLOCKED
set -euo pipefail

TASKS_DIR="${1:-tasks}"

if [ ! -d "$TASKS_DIR" ]; then
  echo "TASKS_TODO=0"
  echo "TASKS_IN_PROGRESS=0"
  echo "TASKS_REVIEW=0"
  echo "TASKS_DONE=0"
  echo "TASKS_BLOCKED=0"
  echo "TASKS_TOTAL=0"
  exit 0
fi

# Find all .md files; if none found, zero out and exit cleanly
FILES=$(find "$TASKS_DIR" -name '*.md' 2>/dev/null)
if [ -z "$FILES" ]; then
  echo "TASKS_TODO=0"
  echo "TASKS_IN_PROGRESS=0"
  echo "TASKS_REVIEW=0"
  echo "TASKS_DONE=0"
  echo "TASKS_BLOCKED=0"
  echo "TASKS_TOTAL=0"
  exit 0
fi

count_status() {
  local status="$1"
  echo "$FILES" | xargs grep -l "\*\*Статус\*\*.*${status}\|\| \*\*Статус\*\* .*${status}\|^| \*\*Статус\*\*.*${status}\|Status.*${status}" 2>/dev/null | wc -l | tr -d ' '
}

TODO=$(echo "$FILES" | xargs grep -rl '| TODO\b\|\*\*Статус\*\*.*TODO\b' 2>/dev/null | wc -l | tr -d ' ')
IN_PROGRESS=$(echo "$FILES" | xargs grep -rl '| IN_PROGRESS\b\|\*\*Статус\*\*.*IN_PROGRESS\b' 2>/dev/null | wc -l | tr -d ' ')
REVIEW=$(echo "$FILES" | xargs grep -rl '| REVIEW\b\|\*\*Статус\*\*.*REVIEW\b' 2>/dev/null | wc -l | tr -d ' ')
DONE=$(echo "$FILES" | xargs grep -rl '| DONE\b\|\*\*Статус\*\*.*DONE\b' 2>/dev/null | wc -l | tr -d ' ')
BLOCKED=$(echo "$FILES" | xargs grep -rl '| BLOCKED\b\|\*\*Статус\*\*.*BLOCKED\b' 2>/dev/null | wc -l | tr -d ' ')
TOTAL=$(echo "$FILES" | wc -l | tr -d ' ')

echo "TASKS_TODO=${TODO:-0}"
echo "TASKS_IN_PROGRESS=${IN_PROGRESS:-0}"
echo "TASKS_REVIEW=${REVIEW:-0}"
echo "TASKS_DONE=${DONE:-0}"
echo "TASKS_BLOCKED=${BLOCKED:-0}"
echo "TASKS_TOTAL=${TOTAL:-0}"
