#!/usr/bin/env bash
# §3.2 — Count Java classes by DDD layers across all Maven modules
# Modules: backend, asset-generator
# Layers: domain | application | infrastructure | presentation
# Counts src/main/java only; test classes reported separately
set -euo pipefail

# ─── Configurable roots ────────────────────────────────────────────
BACKEND_SRC="${BACKEND_SRC:-backend/src/main/java}"
BACKEND_TEST="${BACKEND_TEST:-backend/src/test/java}"
ASSET_GEN_SRC="${ASSET_GEN_SRC:-asset-generator/src/main/java}"

count_java_in() {
  local dir="$1"
  if [ ! -d "$dir" ]; then
    echo 0
    return
  fi
  find "$dir" -name '*.java' 2>/dev/null | wc -l | tr -d ' '
}

# Count DDD layer across all src/main roots by finding all dirs named <layer>
count_layer_across_mains() {
  local layer="$1"
  local total=0
  for src_root in "$BACKEND_SRC" "$ASSET_GEN_SRC"; do
    [ -d "$src_root" ] || continue
    while IFS= read -r -d '' layer_dir; do
      cnt=$(find "$layer_dir" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
      total=$((total + cnt))
    done < <(find "$src_root" -type d -iname "$layer" -print0 2>/dev/null)
  done
  echo "$total"
}

DOMAIN=$(count_layer_across_mains 'domain')
APPLICATION=$(count_layer_across_mains 'application')
INFRASTRUCTURE=$(count_layer_across_mains 'infrastructure')
PRESENTATION=$(count_layer_across_mains 'presentation')

# Total main-source Java files across both modules
BACKEND_MAIN_TOTAL=$(count_java_in "$BACKEND_SRC")
ASSET_GEN_TOTAL=$(count_java_in "$ASSET_GEN_SRC")
TOTAL=$((BACKEND_MAIN_TOTAL + ASSET_GEN_TOTAL))

# Test classes (informational)
BACKEND_TEST_TOTAL=$(count_java_in "$BACKEND_TEST")

echo "BACKEND_DOMAIN=${DOMAIN}"
echo "BACKEND_APPLICATION=${APPLICATION}"
echo "BACKEND_INFRASTRUCTURE=${INFRASTRUCTURE}"
echo "BACKEND_PRESENTATION=${PRESENTATION}"
echo "BACKEND_TOTAL=${TOTAL}"
echo "BACKEND_TEST_TOTAL=${BACKEND_TEST_TOTAL}"
echo "ASSET_GENERATOR_TOTAL=${ASSET_GEN_TOTAL}"
