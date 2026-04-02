#!/usr/bin/env bash
# TASK-BE-023 | §3.6 — Scan generated-assets/ and write assets-manifest.json
# §4.2 schema: { assets[], fallback, totalAssets, generatedAt }
set -euo pipefail

ASSETS_DIR="${ASSETS_DIR:-generated-assets}"
OUTPUT_FILE="${OUTPUT_FILE:-assets-manifest.json}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# ─────────────────────────────────────────────
# Fallback: directory missing or empty
# ─────────────────────────────────────────────
fallback_manifest() {
  jq -n \
    --arg ts "$TIMESTAMP" \
    '{"assets": [], "fallback": true, "totalAssets": 0, "generatedAt": $ts}' \
    > "$OUTPUT_FILE"
  echo "assets-manifest.json written (fallback=true, totalAssets=0)"
  exit 0
}

if [ ! -d "$ASSETS_DIR" ]; then
  fallback_manifest
fi

# Collect all PNG files
PNG_FILES=$(find "$ASSETS_DIR" -name '*.png' 2>/dev/null | sort)

if [ -z "$PNG_FILES" ]; then
  fallback_manifest
fi

# ─────────────────────────────────────────────
# Build JSON array of asset entries
# ─────────────────────────────────────────────
# §4.2 per-asset schema:
# {
#   "id":       "tatyana-idle",
#   "category": "characters",
#   "path":     "assets/characters/tatyana-idle.png",
#   "hasAtlas": false,
#   "frames":   []
# }

ARRAY='[]'

while IFS= read -r PNG_PATH; do
  FILENAME=$(basename "$PNG_PATH")            # tatyana-idle.png
  ID="${FILENAME%.png}"                        # tatyana-idle

  # Category = first path segment after ASSETS_DIR/
  # e.g. generated-assets/characters/tatyana-idle.png → characters
  RELATIVE="${PNG_PATH#${ASSETS_DIR}/}"        # characters/tatyana-idle.png
  CATEGORY=$(echo "$RELATIVE" | cut -d'/' -f1) # characters

  # Normalise: if no subdirectory the category equals the filename → mark as 'unknown'
  if [ "$CATEGORY" = "$FILENAME" ]; then
    CATEGORY="unknown"
  fi

  # Validate known categories; keep as-is otherwise (extensible)
  case "$CATEGORY" in
    characters|locations|items|ui|unknown) ;;
    *) ;; # accept any future category without error
  esac

  # output/assets/<category>/<file> — relative from output/ root (§4.2 path format)
  ASSET_PATH="assets/${CATEGORY}/${FILENAME}"

  # Check for sprite-atlas.json in the same directory as the PNG
  PNG_DIR=$(dirname "$PNG_PATH")
  ATLAS_FILE="${PNG_DIR}/sprite-atlas.json"

  HAS_ATLAS=false
  FRAMES='[]'

  if [ -f "$ATLAS_FILE" ]; then
    HAS_ATLAS=true
    # Read frames array via jq; fall back to [] on parse error
    FRAMES=$(jq -c '.frames // []' "$ATLAS_FILE" 2>/dev/null || echo '[]')
  fi

  # Build one entry and append to the array
  ENTRY=$(jq -n \
    --arg     id       "$ID"          \
    --arg     category "$CATEGORY"    \
    --arg     path     "$ASSET_PATH"  \
    --argjson hasAtlas $HAS_ATLAS     \
    --argjson frames   "$FRAMES"      \
    '{
      "id":       $id,
      "category": $category,
      "path":     $path,
      "hasAtlas": $hasAtlas,
      "frames":   $frames
    }')

  ARRAY=$(echo "$ARRAY" | jq --argjson entry "$ENTRY" '. + [$entry]')
done <<< "$PNG_FILES"

TOTAL=$(echo "$ARRAY" | jq 'length')

jq -n \
  --argjson assets "$ARRAY" \
  --argjson total  "$TOTAL" \
  --arg     ts     "$TIMESTAMP" \
  '{
    "assets":       $assets,
    "fallback":     false,
    "totalAssets":  $total,
    "generatedAt":  $ts
  }' > "$OUTPUT_FILE"

echo "assets-manifest.json written (fallback=false, totalAssets=${TOTAL})"
