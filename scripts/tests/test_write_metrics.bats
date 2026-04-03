#!/usr/bin/env bats
# Тесты scripts/write-metrics.sh

setup() {
  load "helpers/fixtures"
  TMPDIR=$(mktemp -d)
  OUTPUT="$TMPDIR/metrics.json"
}

teardown() {
  rm -rf "$TMPDIR"
}

_run_with_defaults() {
  OUTPUT_FILE="$OUTPUT" \
  GIT_SHA="abc123def456" \
  GIT_BRANCH="main" \
  TASKS_IN_PROGRESS=2 TASKS_DONE=10 TASKS_TOTAL=12 \
  TASKS_BACKEND=5 TASKS_NARRATIVE=3 TASKS_FRONTEND=2 TASKS_ASSETS=1 TASKS_ANALYTICS=1 \
  BACKEND_DOMAIN=4 BACKEND_APPLICATION=3 BACKEND_INFRASTRUCTURE=5 BACKEND_PRESENTATION=2 \
  BACKEND_TOTAL=14 BACKEND_TEST_TOTAL=6 ASSET_GENERATOR_TOTAL=3 \
  NARRATIVE_QUESTS=2 NARRATIVE_NPCS=1 NARRATIVE_WORLD_EVENTS=1 NARRATIVE_CONFLICTS=1 \
  NARRATIVE_WORLD=1 NARRATIVE_ENDINGS=1 NARRATIVE_PLAYER_ACTIONS=1 NARRATIVE_REVIEWS=1 \
  NARRATIVE_ASSET_SPECS=2 NARRATIVE_TOTAL=8 \
  CI_STATUS="completed" CI_CONCLUSION="success" CI_TOTAL_RUNS=5 \
  CI_FAILED_CHECKS=0 CI_SUCCESS_CHECKS=5 \
  run bash "$SCRIPTS_DIR/write-metrics.sh"
}

@test "write-metrics: записывает валидный JSON" {
  _run_with_defaults
  run jq empty "$OUTPUT"
  [ "$status" -eq 0 ]
}

@test "write-metrics: git.short_sha обрезается до 8 символов" {
  _run_with_defaults
  run jq -r '.git.short_sha' "$OUTPUT"
  [ "${#output}" -eq 8 ]
}

@test "write-metrics: tasks.done = 10" {
  _run_with_defaults
  run jq '.tasks.done' "$OUTPUT"
  [ "$output" = "10" ]
}

@test "write-metrics: backend.test_total присутствует" {
  _run_with_defaults
  run jq '.backend.test_total' "$OUTPUT"
  [ "$output" = "6" ]
}

@test "write-metrics: backend.asset_generator присутствует" {
  _run_with_defaults
  run jq '.backend.asset_generator' "$OUTPUT"
  [ "$output" = "3" ]
}

@test "write-metrics: ci.failed_checks присутствует" {
  _run_with_defaults
  run jq '.ci.failed_checks' "$OUTPUT"
  [ "$output" = "0" ]
}

@test "write-metrics: ci.success_checks присутствует" {
  _run_with_defaults
  run jq '.ci.success_checks' "$OUTPUT"
  [ "$output" = "5" ]
}

@test "write-metrics: пустые переменные дают 0 без ошибки" {
  OUTPUT_FILE="$OUTPUT" run bash "$SCRIPTS_DIR/write-metrics.sh"
  [ "$status" -eq 0 ]
  run jq '.tasks.total' "$OUTPUT"
  [ "$output" = "0" ]
}
