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

# Фикстура: backend/done×2 + narrative/done×1 + assets/done×1 = DONE=4
@test "count-tasks: возвращает TASKS_DONE=4" {
  _run_count_tasks
  [[ "$output" == *"TASKS_DONE=4"* ]]
}

# Фикстура: backend/in_progress×1 + frontend/in_progress×1 = IN_PROGRESS=2
@test "count-tasks: возвращает TASKS_IN_PROGRESS=2" {
  _run_count_tasks
  [[ "$output" == *"TASKS_IN_PROGRESS=2"* ]]
}

@test "count-tasks: TASKS_TOTAL равен DONE + IN_PROGRESS = 6" {
  _run_count_tasks
  [[ "$output" == *"TASKS_TOTAL=6"* ]]
}

@test "count-tasks: TASKS_BACKEND считает только backend (3 файла)" {
  _run_count_tasks
  [[ "$output" == *"TASKS_BACKEND=3"* ]]
}

@test "count-tasks: пустая директория даёт нулевые значения" {
  run bash "$SCRIPTS_DIR/count-tasks.sh" "/nonexistent"
  [ "$status" -eq 0 ]
  [[ "$output" == *"TASKS_TOTAL=0"* ]]
}

# .gitkeep добавляется, но TASKS_DONE остается 4 (не считается)
@test "count-tasks: .gitkeep не считается как задача" {
  touch "$TMPDIR/backend/done/.gitkeep"
  _run_count_tasks
  [[ "$output" == *"TASKS_DONE=4"* ]]
}
