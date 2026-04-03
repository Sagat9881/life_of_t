#!/usr/bin/env python3
"""parse_manifest.py — единый источник списка сущностей для CI.

Использование:
    python3 scripts/ci/parse_manifest.py <manifest_path>

Вывод (stdout): одна строка на сущность, формат:
    <entity_id> <type_prefix> <expected_png_filename>

Пример:
    tanya      characters tanya_idle.png
    sam        characters sam_idle.png
    bed        furniture  bed_static.png
    home_room  locations  home_room_bg.png
    hud        ui         hud_default.png

Код возврата:
    0 — манифест успешно разобран
    1 — файл не найден или невалиден
"""
import sys
import os
import xml.etree.ElementTree as ET

# Таблица именования анимаций по типу сущности (ADR-001 / ci-fix-asset-hardcode.md §4.2)
ANIM_BY_TYPE = {
    "characters": "idle",
    "furniture":  "static",
    "locations":  "bg",
    "ui":         "default",
}
DEFAULT_ANIM = "static"


def main():
    if len(sys.argv) < 2:
        print("Usage: parse_manifest.py <manifest_path>", file=sys.stderr)
        sys.exit(1)

    manifest_path = sys.argv[1]
    if not os.path.isfile(manifest_path):
        print(f"ERROR: manifest not found: {manifest_path}", file=sys.stderr)
        sys.exit(1)

    try:
        tree = ET.parse(manifest_path)
    except ET.ParseError as e:
        print(f"ERROR: invalid XML in manifest: {e}", file=sys.stderr)
        sys.exit(1)

    root = tree.getroot()
    entities = root.findall("entity")
    if not entities:
        # Try nested structure <manifest><entities><entity .../></entities></manifest>
        entities = root.findall(".//entity")

    count = 0
    for entity in entities:
        abstract = entity.get("abstract", "false").strip().lower()
        if abstract == "true":
            continue
        path = entity.get("path", "").strip()
        if not path:
            continue
        segments = [s for s in path.split("/") if s]
        entity_id   = segments[-1] if segments else path
        type_prefix = segments[0]  if len(segments) > 1 else "unknown"
        anim_name   = ANIM_BY_TYPE.get(type_prefix, DEFAULT_ANIM)
        png_name    = f"{entity_id}_{anim_name}.png"
        print(f"{entity_id}\t{type_prefix}\t{png_name}")
        count += 1

    print(f"# total_entities={count}", file=sys.stderr)


if __name__ == "__main__":
    main()
