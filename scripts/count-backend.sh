#!/usr/bin/env bash
# §3.2 — Count Java classes by DDD layers
# Layers: domain | application | infrastructure | presentation
set -euo pipefail

BACKEND_DIR="${1:-backend/src/main/java}"

count_layer() {
  local layer_path="$1"
  if [ ! -d "$layer_path" ]; then
    echo 0
    return
  fi
  find "$layer_path" -name '*.java' 2>/dev/null | wc -l | tr -d ' '
}

# Support both flat-layout and package-based layout
if [ -d "$BACKEND_DIR" ]; then
  BASE="$BACKEND_DIR"
else
  BASE="backend"
fi

# Try standard DDD layer dirs (ru.lifegame.backend.*)
DOMAIN=$(find "$BASE" -type d \( -name 'domain' -o -name 'Domain' \) 2>/dev/null \
  | head -1 | xargs -I{} find {} -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
APPLICATION=$(find "$BASE" -type d \( -name 'application' -o -name 'Application' \) 2>/dev/null \
  | head -1 | xargs -I{} find {} -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
INFRASTRUCTURE=$(find "$BASE" -type d \( -name 'infrastructure' -o -name 'Infrastructure' \) 2>/dev/null \
  | head -1 | xargs -I{} find {} -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
PRESENTATION=$(find "$BASE" -type d \( -name 'presentation' -o -name 'Presentation' -o -name 'controller' \) 2>/dev/null \
  | head -1 | xargs -I{} find {} -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
TOTAL=$(find "$BASE" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')

echo "BACKEND_DOMAIN=${DOMAIN:-0}"
echo "BACKEND_APPLICATION=${APPLICATION:-0}"
echo "BACKEND_INFRASTRUCTURE=${INFRASTRUCTURE:-0}"
echo "BACKEND_PRESENTATION=${PRESENTATION:-0}"
echo "BACKEND_TOTAL=${TOTAL:-0}"
