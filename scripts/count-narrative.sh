#!/usr/bin/env bash
# §3.3 — Count XML narrative specs by directory
# Real path: game-content/life-of-t/src/main/resources/narrative/
# Directories: quests | npc-behavior | events | confilcts | world | endings | player-actions | reviews
# Note: 'confilcts' is the actual directory name in the repo (typo preserved intentionally)
set -euo pipefail

CONTENT_DIR="${1:-game-content}"

# Resolve Maven resource path
NARRATIVE_BASE="${CONTENT_DIR}/life-of-t/src/main/resources/narrative"
ASSET_SPECS_DIR="${CONTENT_DIR}/life-of-t/src/main/resources/asset-specs"

count_xml_in() {
  local dir="$1"
  if [ ! -d "$dir" ]; then
    echo 0
    return
  fi
  find "$dir" -name '*.xml' 2>/dev/null | wc -l | tr -d ' '
}

if [ ! -d "$NARRATIVE_BASE" ]; then
  echo "NARRATIVE_QUESTS=0"
  echo "NARRATIVE_NPCS=0"
  echo "NARRATIVE_WORLD_EVENTS=0"
  echo "NARRATIVE_CONFLICTS=0"
  echo "NARRATIVE_WORLD=0"
  echo "NARRATIVE_ENDINGS=0"
  echo "NARRATIVE_PLAYER_ACTIONS=0"
  echo "NARRATIVE_REVIEWS=0"
  echo "NARRATIVE_ASSET_SPECS=0"
  echo "NARRATIVE_TOTAL=0"
  exit 0
fi

QUESTS=$(count_xml_in "${NARRATIVE_BASE}/quests")
NPCS=$(count_xml_in "${NARRATIVE_BASE}/npc-behavior")       # real dir: npc-behavior
WORLD_EVENTS=$(count_xml_in "${NARRATIVE_BASE}/events")     # real dir: events
CONFLICTS=$(count_xml_in "${NARRATIVE_BASE}/confilcts")     # real dir: confilcts (typo in repo)
WORLD=$(count_xml_in "${NARRATIVE_BASE}/world")
ENDINGS=$(count_xml_in "${NARRATIVE_BASE}/endings")
PLAYER_ACTIONS=$(count_xml_in "${NARRATIVE_BASE}/player-actions")
REVIEWS=$(count_xml_in "${NARRATIVE_BASE}/reviews")
ASSET_SPECS=$(count_xml_in "${ASSET_SPECS_DIR}")

TOTAL=$(find "$NARRATIVE_BASE" -name '*.xml' 2>/dev/null | wc -l | tr -d ' ')

echo "NARRATIVE_QUESTS=${QUESTS}"
echo "NARRATIVE_NPCS=${NPCS}"
echo "NARRATIVE_WORLD_EVENTS=${WORLD_EVENTS}"
echo "NARRATIVE_CONFLICTS=${CONFLICTS}"
echo "NARRATIVE_WORLD=${WORLD}"
echo "NARRATIVE_ENDINGS=${ENDINGS}"
echo "NARRATIVE_PLAYER_ACTIONS=${PLAYER_ACTIONS}"
echo "NARRATIVE_REVIEWS=${REVIEWS}"
echo "NARRATIVE_ASSET_SPECS=${ASSET_SPECS}"
echo "NARRATIVE_TOTAL=${TOTAL}"
