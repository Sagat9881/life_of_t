#!/usr/bin/env bats
# Тесты scripts/count-backend.sh

setup() {
  load "helpers/fixtures"
  TMPDIR=$(mktemp -d)
  make_backend_fixture "$TMPDIR/backend"
}

teardown() {
  rm -rf "$TMPDIR"
}

_run() {
  BACKEND_SRC="$TMPDIR/backend/src/main/java" \
  BACKEND_TEST="$TMPDIR/backend/src/test/java" \
  ASSET_GEN_SRC="$TMPDIR/asset-generator/src/main/java" \
  run bash "$SCRIPTS_DIR/count-backend.sh"
}

@test "count-backend: считает domain" {
  _run
  [[ "$output" == *"BACKEND_DOMAIN=3"* ]]
}

@test "count-backend: считает application" {
  _run
  [[ "$output" == *"BACKEND_APPLICATION=1"* ]]
}

@test "count-backend: считает тесты отдельно" {
  _run
  [[ "$output" == *"BACKEND_TEST_TOTAL=1"* ]]
}

@test "count-backend: ASSET_GENERATOR_TOTAL считает модуль" {
  _run
  [[ "$output" == *"ASSET_GENERATOR_TOTAL=1"* ]]
}

@test "count-backend: BACKEND_TOTAL включает asset-generator" {
  _run
  [[ "$output" == *"BACKEND_TOTAL=6"* ]]
}

@test "count-backend: отсутствие src даёт нулевые значения" {
  BACKEND_SRC="/nonexistent" \
  BACKEND_TEST="/nonexistent" \
  ASSET_GEN_SRC="/nonexistent" \
  run bash "$SCRIPTS_DIR/count-backend.sh"
  [ "$status" -eq 0 ]
  [[ "$output" == *"BACKEND_TOTAL=0"* ]]
}
