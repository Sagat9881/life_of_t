#!/usr/bin/env bats
# Тесты scripts/count-tasks.sh

setup() {
  load "helpers/fixtures"
  TMPDIR=$(mktemp -d)
  make_tasks_fixture "$TMPDIR"
}

teardown() {
  rm -rf "$TMPDIR"
}

_run_count_tasks() {
  run bash "$SCRIPTS_DIR/count-tasks.sh" "$TMPDIR"
}

@test "count-tasks: возвращает ненулевые TASKS_DONE" {
  _run_count_tasks
  [[ "$output" == *"TASKS_DONE=3"* ]]
}

@test "count-tasks: возвращает ненулевые TASKS_IN_PROGRESS" {
  _run_count_tasks
  [[ "$output" == *"TASKS_IN_PROGRESS=2"* ]]
}

@test "count-tasks: TASKS_TOTAL равен DONE + IN_PROGRESS" {
  _run_count_tasks
  [[ "$output" == *"TASKS_TOTAL=5"* ]]
}

@test "count-tasks: TASKS_BACKEND считает только backend" {
  _run_count_tasks
  [[ "$output" == *"TASKS_BACKEND=3"* ]]
}

@test "count-tasks: пустая директория даёт нулевые значения" {
  run bash "$SCRIPTS_DIR/count-tasks.sh" "/nonexistent"
  [ "$status" -eq 0 ]
  [[ "$output" == *"TASKS_TOTAL=0"* ]]
}

@test "count-tasks: .gitkeep не считается как задача" {
  touch "$TMPDIR/backend/done/.gitkeep"
  _run_count_tasks
  [[ "$output" == *"TASKS_DONE=3"* ]]
}
