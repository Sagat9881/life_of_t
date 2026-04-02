#!/usr/bin/env bash
# §3.3 — Count XML narrative specs by directory
# Directories: quests | npcs | world-events | conflicts
set -euo pipefail

CONTENT_DIR="${1:-game-content}"

count_xml_in() {
  local dir="$1"
  if [ ! -d "$dir" ]; then
    echo 0
    return
  fi
  find "$dir" -name '*.xml' 2>/dev/null | wc -l | tr -d ' '
}

QUESTS=$(count_xml_in "${CONTENT_DIR}/quests")
NPCS=$(count_xml_in "${CONTENT_DIR}/npcs")
WORLD_EVENTS=$(count_xml_in "${CONTENT_DIR}/world-events")
CONFLICTS=$(count_xml_in "${CONTENT_DIR}/conflicts")
TOTAL=$(find "$CONTENT_DIR" -name '*.xml' 2>/dev/null | wc -l | tr -d ' ')

echo "NARRATIVE_QUESTS=${QUESTS:-0}"
echo "NARRATIVE_NPCS=${NPCS:-0}"
echo "NARRATIVE_WORLD_EVENTS=${WORLD_EVENTS:-0}"
echo "NARRATIVE_CONFLICTS=${CONFLICTS:-0}"
echo "NARRATIVE_TOTAL=${TOTAL:-0}"
