#!/usr/bin/env bats
# Тесты scripts/fetch-ci-status.sh (без реальных API-запросов)

setup() {
  load "helpers/fixtures"
}

@test "fetch-ci-status: без GH_TOKEN возвращает unknown" {
  GH_TOKEN="" GITHUB_TOKEN="" CI_REF="abc123" \
  run bash "$SCRIPTS_DIR/fetch-ci-status.sh"
  [ "$status" -eq 0 ]
  [[ "$output" == *"CI_STATUS=unknown"* ]]
}

@test "fetch-ci-status: HEAD в CI_REF возвращает unknown" {
  GH_TOKEN="dummy" CI_REF="HEAD" \
  run bash "$SCRIPTS_DIR/fetch-ci-status.sh"
  [ "$status" -eq 0 ]
  [[ "$output" == *"CI_STATUS=unknown"* ]]
}

@test "fetch-ci-status: пустой CI_REF возвращает unknown" {
  GH_TOKEN="dummy" CI_REF="" GITHUB_SHA="" \
  run bash "$SCRIPTS_DIR/fetch-ci-status.sh"
  [ "$status" -eq 0 ]
  [[ "$output" == *"CI_STATUS=unknown"* ]]
}

@test "fetch-ci-status: вывод содержит CI_FAILED_CHECKS" {
  GH_TOKEN="" GITHUB_TOKEN="" CI_REF="abc" \
  run bash "$SCRIPTS_DIR/fetch-ci-status.sh"
  [[ "$output" == *"CI_FAILED_CHECKS="* ]]
}

@test "fetch-ci-status: вывод содержит CI_SUCCESS_CHECKS" {
  GH_TOKEN="" GITHUB_TOKEN="" CI_REF="abc" \
  run bash "$SCRIPTS_DIR/fetch-ci-status.sh"
  [[ "$output" == *"CI_SUCCESS_CHECKS="* ]]
}
