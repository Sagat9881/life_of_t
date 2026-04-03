#!/usr/bin/env bash
# TASK-BE-023 | §3.6 — Scan generated-assets/ and write assets-manifest.json
# §4.2 schema: { assets[], fallback, totalAssets, generatedAt }
# Supported formats: PNG, WebP, JPEG, GIF, SVG
# Performance: O(n) — single jq -s call at the end instead of O(n²) accumulation
set -euo pipefail

ASSETS_DIR="${ASSETS_DIR:-generated-assets}"
OUTPUT_FILE="${OUTPUT_FILE:-assets-manifest.json}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

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

# Collect all supported asset files (PNG, WebP, JPEG, GIF, SVG)
mapfile -d '' ASSET_FILES < <(
  find "$ASSETS_DIR" \( \
    -iname '*.png' -o \
    -iname '*.webp' -o \
    -iname '*.jpg' -o \
    -iname '*.jpeg' -o \
    -iname '*.gif' -o \
    -iname '*.svg' \
  \) -print0 2>/dev/null | sort -z
)

if [ ${#ASSET_FILES[@]} -eq 0 ]; then
  fallback_manifest
fi

# ─────────────────────────────────────────────
# Build one JSON object per asset, pipe all to jq -s at the end (O(n))
# ─────────────────────────────────────────────
build_entries() {
  for ASSET_PATH in "${ASSET_FILES[@]}"; do
    FILENAME=$(basename "$ASSET_PATH")
    # Strip extension to get id
    ID="${FILENAME%.*}"

    # Category = first path segment after ASSETS_DIR/
    RELATIVE="${ASSET_PATH#${ASSETS_DIR}/}"
    CATEGORY=$(echo "$RELATIVE" | cut -d'/' -f1)
    if [ "$CATEGORY" = "$FILENAME" ]; then
      CATEGORY="unknown"
    fi

    # Derive format from extension
    EXT="${FILENAME##*.}"
    FORMAT=$(echo "$EXT" | tr '[:upper:]' '[:lower:]')
    case "$FORMAT" in
      jpg) FORMAT="jpeg" ;;
    esac

    ASSET_OUT_PATH="assets/${CATEGORY}/${FILENAME}"

    # Check for sprite-atlas.json next to the file
    ATLAS_FILE="$(dirname "$ASSET_PATH")/sprite-atlas.json"
    HAS_ATLAS=false
    FRAMES='[]'
    if [ -f "$ATLAS_FILE" ]; then
      HAS_ATLAS=true
      FRAMES=$(jq -c '.frames // []' "$ATLAS_FILE" 2>/dev/null || echo '[]')
    fi

    jq -n \
      --arg     id        "$ID"           \
      --arg     category  "$CATEGORY"     \
      --arg     path      "$ASSET_OUT_PATH" \
      --arg     format    "$FORMAT"       \
      --argjson hasAtlas  $HAS_ATLAS      \
      --argjson frames    "$FRAMES"       \
      '{"id":$id,"category":$category,"path":$path,"format":$format,"hasAtlas":$hasAtlas,"frames":$frames}'
  done
}

# Single jq -s call: collect all newline-separated JSON objects into array
build_entries | jq -s \
  --arg ts "$TIMESTAMP" \
  '{
    "assets":      .,
    "fallback":    false,
    "totalAssets": length,
    "generatedAt": $ts
  }' > "$OUTPUT_FILE"

TOTAL=$(jq '.totalAssets' "$OUTPUT_FILE")
echo "assets-manifest.json written (fallback=false, totalAssets=${TOTAL})"
