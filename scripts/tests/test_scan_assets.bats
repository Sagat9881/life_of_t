#!/usr/bin/env bats
# Тесты scripts/scan-assets.sh

setup() {
  load "helpers/fixtures"
  TMPDIR=$(mktemp -d)
  make_assets_fixture "$TMPDIR/assets"
  OUTPUT="$TMPDIR/assets-manifest.json"
}

teardown() {
  rm -rf "$TMPDIR"
}

_run() {
  ASSETS_DIR="$TMPDIR/assets" OUTPUT_FILE="$OUTPUT" \
  run bash "$SCRIPTS_DIR/scan-assets.sh"
}

@test "scan-assets: записывает валидный JSON" {
  _run
  run jq empty "$OUTPUT"
  [ "$status" -eq 0 ]
}

@test "scan-assets: totalAssets = 3" {
  _run
  run jq '.totalAssets' "$OUTPUT"
  [ "$output" = "3" ]
}

@test "scan-assets: fallback = false при наличии ассетов" {
  _run
  run jq '.fallback' "$OUTPUT"
  [ "$output" = "false" ]
}

@test "scan-assets: ассет с atlas имеет hasAtlas=true" {
  _run
  run jq '[.assets[] | select(.hasAtlas == true)] | length' "$OUTPUT"
  [ "$output" = "2" ]
}

@test "scan-assets: format WebP распознаётся" {
  _run
  run jq '[.assets[] | select(.format == "webp")] | length' "$OUTPUT"
  [ "$output" = "1" ]
}

@test "scan-assets: пустая директория даёт fallback=true" {
  ASSETS_DIR="/nonexistent" OUTPUT_FILE="$OUTPUT" \
  run bash "$SCRIPTS_DIR/scan-assets.sh"
  [ "$status" -eq 0 ]
  run jq '.fallback' "$OUTPUT"
  [ "$output" = "true" ]
}

@test "scan-assets: путь ассета содержит category" {
  _run
  run jq '[.assets[] | select(.category == "characters")] | length' "$OUTPUT"
  [ "$output" = "2" ]
}
