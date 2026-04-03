#!/usr/bin/env bats
# Тесты scripts/lib.sh

setup() {
  load "helpers/fixtures"
  source "$SCRIPTS_DIR/lib.sh"
  TMPDIR=$(mktemp -d)
}

teardown() {
  rm -rf "$TMPDIR"
}

@test "val_int: целое число проходит валидацию" {
  run val_int "42"
  [ "$output" = "42" ]
}

@test "val_int: пустая строка даёт 0" {
  run val_int ""
  [ "$output" = "0" ]
}

@test "val_int: нецелое значение даёт 0" {
  run val_int "abc"
  [ "$output" = "0" ]
}

@test "count_files: считает файлы по паттерну" {
  touch "$TMPDIR/a.xml" "$TMPDIR/b.xml" "$TMPDIR/c.md"
  run count_files "$TMPDIR" '*.xml'
  [ "$output" = "2" ]
}

@test "count_files: несуществующая директория даёт 0" {
  run count_files "/nonexistent/path" '*.xml'
  [ "$output" = "0" ]
}

@test "count_xml_in: считает XML" {
  touch "$TMPDIR/a.xml" "$TMPDIR/b.xml"
  run count_xml_in "$TMPDIR"
  [ "$output" = "2" ]
}

@test "count_java_in: считает Java" {
  touch "$TMPDIR/A.java" "$TMPDIR/B.java" "$TMPDIR/C.java"
  run count_java_in "$TMPDIR"
  [ "$output" = "3" ]
}
