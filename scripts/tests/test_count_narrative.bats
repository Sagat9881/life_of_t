#!/usr/bin/env bats
# Тесты scripts/count-narrative.sh

setup() {
  load "helpers/fixtures"
  TMPDIR=$(mktemp -d)
  make_narrative_fixture "$TMPDIR"
}

teardown() {
  rm -rf "$TMPDIR"
}

_run() {
  run bash "$SCRIPTS_DIR/count-narrative.sh" "$TMPDIR"
}

@test "count-narrative: считает quests" {
  _run
  [[ "$output" == *"NARRATIVE_QUESTS=2"* ]]
}

@test "count-narrative: считает npc-behavior" {
  _run
  [[ "$output" == *"NARRATIVE_NPCS=1"* ]]
}

@test "count-narrative: считает events" {
  _run
  [[ "$output" == *"NARRATIVE_WORLD_EVENTS=1"* ]]
}

@test "count-narrative: считает confilcts (опечатка директории сохранена)" {
  _run
  [[ "$output" == *"NARRATIVE_CONFLICTS=1"* ]]
}

@test "count-narrative: считает asset-specs" {
  _run
  [[ "$output" == *"NARRATIVE_ASSET_SPECS=2"* ]]
}

# NARRATIVE_TOTAL = считает только narrative/ без asset-specs
# quests×2 + npc-behavior×1 + events×1 + confilcts×1 + world×1 + endings×1 + player-actions×1 + reviews×1 = 9
@test "count-narrative: NARRATIVE_TOTAL равен сумме XML в narrative/" {
  _run
  [[ "$output" == *"NARRATIVE_TOTAL=9"* ]]
}

@test "count-narrative: отсутствие директории даёт нулевые значения" {
  run bash "$SCRIPTS_DIR/count-narrative.sh" "/nonexistent"
  [ "$status" -eq 0 ]
  [[ "$output" == *"NARRATIVE_TOTAL=0"* ]]
}
