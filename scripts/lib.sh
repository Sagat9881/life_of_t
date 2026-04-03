#!/usr/bin/env bash
# scripts/lib.sh — Общие утилиты для всех scripts/
# Использование: source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

# ─── Счёт файлов ────────────────────────────────────────────────────────

# count_files <dir> <pattern>
# Считает файлы по паттерну в директории (рекурсивно). Возвращает 0 если дир не существует.
count_files() {
  local dir="$1" pattern="$2"
  if [ ! -d "$dir" ]; then echo 0; return; fi
  find "$dir" -name "$pattern" 2>/dev/null | wc -l | tr -d ' '
}

# count_xml_in <dir>
# Считает XML-файлы в директории. Возвращает 0 если дир не существует.
count_xml_in() {
  count_files "$1" '*.xml'
}

# count_java_in <dir>
# Считает Java-файлы в директории. Возвращает 0 если дир не существует.
count_java_in() {
  count_files "$1" '*.java'
}

# ─── Валидация данных ────────────────────────────────────────────────

# val_int <value>
# Возвращает значение если целое число, иначе 0. Защищает jq от пустых строк.
val_int() {
  local v="${1:-0}"
  [[ "$v" =~ ^[0-9]+$ ]] && echo "$v" || echo "0"
}

# ─── Логирование ─────────────────────────────────────────────────────

log_info()  { echo "[INFO]  $*" >&2; }
log_warn()  { echo "[WARN]  $*" >&2; }
log_error() { echo "[ERROR] $*" >&2; }

# ─── Общий фаллбек JSON ──────────────────────────────────────────────

# fallback_json <output_file> <extra_jq_args...>
# Записывает минимальный fallback-объект { "fallback": true, "generatedAt": <ts> } в файл.
fallback_json() {
  local out="$1"
  local ts
  ts=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  jq -n --arg ts "$ts" '{"fallback":true,"generatedAt":$ts}' > "$out"
}
