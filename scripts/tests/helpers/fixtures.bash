#!/usr/bin/env bash
# helpers/fixtures.bash — Создание и уборка fixtures для bats-тестов

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# ─── Структура tasks/ ─────────────────────────────────────────────
make_tasks_fixture() {
  local base="$1"
  mkdir -p \
    "$base/backend/done" \
    "$base/backend/in_progress" \
    "$base/narrative/done" \
    "$base/frontend/in_progress" \
    "$base/assets/done"
  touch \
    "$base/backend/done/TASK-BE-001.md" \
    "$base/backend/done/TASK-BE-002.md" \
    "$base/backend/in_progress/TASK-BE-003.md" \
    "$base/narrative/done/TASK-NA-001.md" \
    "$base/frontend/in_progress/TASK-FE-001.md" \
    "$base/assets/done/TASK-AS-001.md"
}

# ─── Структура narrative/ ─────────────────────────────────────────
make_narrative_fixture() {
  local base="$1"
  local res="$base/life-of-t/src/main/resources"
  mkdir -p \
    "$res/narrative/quests" \
    "$res/narrative/npc-behavior" \
    "$res/narrative/events" \
    "$res/narrative/confilcts" \
    "$res/narrative/world" \
    "$res/narrative/endings" \
    "$res/narrative/player-actions" \
    "$res/narrative/reviews" \
    "$res/asset-specs"
  touch \
    "$res/narrative/quests/q1.xml" \
    "$res/narrative/quests/q2.xml" \
    "$res/narrative/npc-behavior/npc1.xml" \
    "$res/narrative/events/ev1.xml" \
    "$res/narrative/confilcts/cf1.xml" \
    "$res/narrative/world/w1.xml" \
    "$res/narrative/endings/end1.xml" \
    "$res/narrative/player-actions/pa1.xml" \
    "$res/narrative/reviews/rv1.xml" \
    "$res/asset-specs/as1.xml" \
    "$res/asset-specs/as2.xml"
}

# ─── Структура backend/ ─────────────────────────────────────────────
make_backend_fixture() {
  local base="$1"
  local src="$base/src/main/java/ru/lifegame"
  local test="$base/src/test/java/ru/lifegame"
  local agen="$base/../asset-generator/src/main/java/ru/lifegame"
  mkdir -p \
    "$src/domain" \
    "$src/application" \
    "$src/infrastructure" \
    "$src/presentation" \
    "$test/domain" \
    "$agen/domain"
  touch \
    "$src/domain/Entity.java" \
    "$src/domain/ValueObject.java" \
    "$src/application/Service.java" \
    "$src/infrastructure/Repo.java" \
    "$src/presentation/Controller.java" \
    "$test/domain/EntityTest.java" \
    "$agen/domain/AssetDomain.java"
}

# ─── Структура generated-assets/ ────────────────────────────────────
make_assets_fixture() {
  local base="$1"
  mkdir -p "$base/characters" "$base/ui"
  touch \
    "$base/characters/tatyana-idle.png" \
    "$base/characters/ivan.webp" \
    "$base/ui/button.png"
  # atlas для первого ассета
  echo '{"frames":["frame_0","frame_1"]}' > "$base/characters/sprite-atlas.json"
}
