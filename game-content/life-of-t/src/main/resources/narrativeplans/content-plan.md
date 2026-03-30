# 📍 Content Plan — Нарративный контент-план «Life of T»

```
block-id:    world / ContentPlan
entity:      ContentPlan
version:     1.0.0
date:        2026-03-30
status:      Draft
author:      Narrator
task:        TASK-NA-011
```

> Этот документ является центральной навигационной картой нарративного контента игры.
> Основан на «tanya-facts.md» (TASK-NA-010):
> `narrative/world/tatyana-facts.md`
>
> Обновляется Нарратором после каждой волны Implement.
> Код и UI не описываются.

**event_triggers для обновления плана:**
- Добавлен новый квест, конфликт, NPC или событие → обновить соответствующую арку и таблицу статусов.
- Концовка переходит в `Approved` → зафиксировать выполненные предусловия в разделе «Статус контента».
- `tatyana-facts.md` обновлён → перечитать связи в разделе «Мета».

---

## 1. Сюжетные арки

> Основание: narrantor-skill.md §6.2 (фаза Plan) — структурирование арк, эмоциональная кривая, роли NPC.

---

### 💼 Арка 1 — Работа и выгорание

**Описание:** Таня работает — иногда слишком. Арка исследует цикл выгорания:
от перегрузки → точки перелома → восстановления.Ведёт к карьерному взлёту или разрушению.

| Параметр | Содержание |
|---------|----------|
| **Квесты** | `quest_work_deadline`, `quest_anxiety_attack`, `quest_separation` |
| **Конфликты** | `burnout`, `identity_crisis`, `guilt` |
| **NPC** | `alexander` (поддержка), `garfield` (утешение) |
| **События** | `event_night_anxiety`, `event_alexander_code`, `event_rain_outside` |
| **Действия** | `go_to_work`, `work_on_project`, `rest_at_home`, `self_care` |
| **Концовки** | `good_career` → позитив; `workaholic_burnout`, `burnout` (ending) → негатив |

**Эмоциональная кривая:** напряжение → точка перелома → усталость → решение (границы / отдых) → подъём

---

### 💞 Арка 2 — Романтика и семья

**Описание:** Отношения Тани и Александра: от повседневной рутины через кризисы
к свадьбе и подлинному счастью. Центральная арка игры.

| Параметр | Содержание |
|---------|----------|
| **Квесты** | `quest_alexander_dinner`, `quest_family_photo`, `quest_wedding`, `quest_separation` |
| **Конфликты** | `romantic_crisis`, `household_duties`, `lack_of_attention`, `financial_disagreement` |
| **NPC** | `alexander` |
| **События** | `event_alexander_dinner` → `quest_alexander_dinner`, `event_alexander_snore`, `event_old_photo`, `event_call_yulia` |
| **Действия** | `date_with_husband`, `cook_food`, `call_husband`, `household` |
| **Концовки** | `family_happiness`, `family_focus` → позитив; `divorce` → негатив |

**Эмоциональная кривая:** тепло → рутина → дистанция → разговор → близость / разрыв

---

### 🐾 Арка 3 — Питомцы

**Описание:** Жизнь зверинца — это параллельный сюжет, пронизывающий всю игру.
События с питомцами — юмор, тепло и драма одновременно.

| Параметр | Содержание |
|---------|----------|
| **Квесты** | `quest_feed_the_pride`, `quest_garfield_comfort`, `quest_find_persi`, `quest_save_lada`, `quest_sam_trust`, `quest_duke_arrives`, `quest_aijan_integration`, `quest_voland_visit` |
| **Конфликты** | прямых конфликтов нет; питомцы влияют на `burnout` и `guilt` косвенно |
| **NPC** | `garfield`, `aijan`, `duke`, `persi`, `klop`, `lada`, `sam`, `thelma`, `cirilla`, `voland` |
| **События** | `event_garfield_purr`, `event_aijan_hiss`, `event_duke_chew`, `event_klop_belly`, `event_persi_wisdom`, `event_thelma_zoomies`, `event_sam_follows_tanya`, `event_cirilla_muselet`, `event_voland_memory` |
| **Действия** | `feed_pets`, `play_with_cat`, `walk_dog` |
| **Концовки** | `balanced_life` (feed_pets + mood), `family_happiness` (питомцы в контексте свадьбы), `neutral_epilogue` |

**Эмоциональная кривая:** хаос / комедия → забота → теплота / смятение

---

### 👨‍👧 Арка 4 — Отец и семья

**Описание:** Отношения Тани с отцом: границы, вина, любовь.
Также сюда относится арка летнего сезона и общей семейной жизни.

| Параметр | Содержание |
|---------|----------|
| **Квесты** | `quest_dacha_summer`, `quest_family_photo` |
| **Конфликты** | `father_concern`, `father_criticism`, `father_neglected`, `guilt` |
| **NPC** | Отец (через конфликты), `alexander` (параллельно) |
| **События** | `event_old_photo`, `event_call_yulia` (поддержка подруги) |
| **Действия** | `visit_father`, `cook_food` (для отца), `eat_food` |
| **Концовки** | `family_focus`, `family_happiness` → позитив; `isolation` → негатив |

**Эмоциональная кривая:** вина → напряжение → разговор / визит → тепло / дистанция

---

### 🌱 Арка 5 — Личностный рост

**Описание:** Таня ищет себя: кто она, чего хочет, как заботиться о себе.
Арка внутреннего путешествия, подпитывающая все остальные.

| Параметр | Содержание |
|---------|----------|
| **Квесты** | `quest_anxiety_attack`, `quest_garfield_comfort`, `quest_find_persi` |
| **Конфликты** | `identity_crisis`, `burnout`, `guilt` |
| **NPC** | `garfield`, `persi` (эмоциональная опора), `alexander` |
| **События** | `event_night_anxiety`, `event_rain_outside`, `event_persi_wisdom`, `event_garfield_purr`, `event_old_photo` |
| **Действия** | `self_care`, `beauty_routine`, `rest_at_home`, `eat_food` |
| **Концовки** | `balanced_life` → позитив; `isolation`, `burnout` (ending) → негатив |

**Эмоциональная кривая:** тревога → поиск опоры → принятие себя → гармония

---

## 2. Карта событий (world-events)

> Основание: narrantor-skill.md §5.4 (world-events) — события мира влияют на Таню и провоцируют квесты/конфликты.

| Событие | Арка | Связь с квестом / конфликтом |
|---------|------|-----------|
| `event_night_anxiety` | Арка 1, 5 | → `quest_anxiety_attack`, → `burnout`, `identity_crisis` |
| `event_alexander_code` | Арка 1, 2 | → предвестник `lack_of_attention`, `romantic_crisis` |
| `event_alexander_snore` | Арка 2 | → бытовой юмор, стабилизатор настроения |
| `event_call_yulia` | Арка 2, 4 | → поддержка `mood`, предвестник `quest_wedding` |
| `event_old_photo` | Арка 2, 4, 5 | → `romantic_crisis` или стабилизатор, яркость воспоминания |
| `event_rain_outside` | Арка 1, 5 | → усиливает `burnout_risk`, амбиент тревоги |
| `event_garfield_purr` | Арка 3, 5 | → `quest_garfield_comfort`, +mood |
| `event_aijan_hiss` | Арка 3 | → `quest_aijan_integration`, бытовой юмор |
| `event_duke_chew` | Арка 3 | → мини-квест домашних обязанностей |
| `event_klop_belly` | Арка 3 | → +mood, стабилизатор |
| `event_persi_wisdom` | Арка 3, 5 | → триггер рефлексии, предвестник `identity_crisis` (resolve) |
| `event_sam_follows_tanya` | Арка 3 | → `quest_sam_trust`, +closeness |
| `event_thelma_zoomies` | Арка 3 | → +mood, бытовой юмор |
| `event_cirilla_muselet` | Арка 3 | → +mood, привязанность котов |
| `event_voland_memory` | Арка 3 | → `quest_voland_visit`, ностальгия |

---

## 3. Карта действий игрока

> Основание: narrantor-skill.md §6.1 (Specify) — действия игрока — это event_triggers для арк / конфликтов.

| Действие | Арка | Влияние на статы / конфликты |
|---------|------|-----------|
| `go_to_work` | Арка 1 | +job_satisfaction или +burnout_risk в зависимости от energy |
| `work_on_project` | Арка 1 | +job_satisfaction, +burnout_risk |
| `rest_at_home` | Арка 1, 5 | -stress, +mood; профилактика `burnout` |
| `self_care` | Арка 5 | +self_esteem, +mood; требуется для `BALANCED_LIFE` |
| `beauty_routine` | Арка 5 | +self_esteem; профилактика `identity_crisis` |
| `cook_food` | Арка 2, 4 | +closeness (Александр), -stress |
| `eat_food` | Арка 4, 5 | +energy, +mood |
| `date_with_husband` | Арка 2 | +romance, +closeness; профилактика `romantic_crisis` |
| `call_husband` | Арка 2 | +closeness; профилактика `lack_of_attention` |
| `household` | Арка 2 | -stress или +stress; триггер `household_duties` |
| `feed_pets` | Арка 3, 5 | +mood, +pet_closeness; базовый стабилизатор |
| `play_with_cat` | Арка 3 | +mood, +cat_closeness |
| `walk_dog` | Арка 3 | +energy, +dog_closeness, -stress |
| `visit_father` | Арка 4 | +/- в зависимости от тактики; триггер `father_*` |

---

## 4. Карта концовок

> Основание: narrantor-skill.md §6.2 (Plan) — связь концовок с арками и условиями.

| Концовка | Название | Категория | Арка | Основное условие |
|----------|---------|---------|------|----------------|
| `balanced_life` | Гармония | STORY | 5 | SELF_CARE_ARC + mood≥70 + self_esteem≥70 |
| `family_happiness` | Семейное счастье | STORY | 2 | closeness≥80 + romance≥70 |
| `family_focus` | Семья в центре | STORY | 2, 4 | выбор в пользу семьи vs карьеры |
| `good_career` | Карьерный взлёт | STORY | 1 | job_satisfaction≥80 + CAREER_GROWTH |
| `neutral_epilogue` | Нейтральный финал | STORY | 3 | всё в норме, ничего не разрешилось |
| `isolation` | Изоляция | GAME_OVER | 5 | total_closeness≤20 |
| `divorce` | Развод | GAME_OVER | 2 | Александр broken=1 |
| `burnout` (ending) | Выгорание | GAME_OVER | 1, 5 | burnout_risk≥90 + stress≥90 |
| `workaholic_burnout` | Переработка | GAME_OVER | 1 | go_to_work/work_on_project >> rest |
| `bankruptcy` | Банкротство | GAME_OVER | 1, 2 | financial_disagreement не разрешён |

---

## 5. Статус контента

> Основание: narrantor-skill.md §6.4 (Implement) — контент-план фиксирует весь контент, его статус и связи.

### Квесты

| Артефакт | Арка | Статус |
|---------|------|---------|
| `quest_aijan_integration` | 3 | Ready |
| `quest_alexander_dinner` | 2 | Ready |
| `quest_anxiety_attack` | 1, 5 | Ready |
| `quest_dacha_summer` | 4 | Ready |
| `quest_duke_arrives` | 3 | Ready |
| `quest_family_photo` | 2, 4 | Ready |
| `quest_feed_the_pride` | 3 | Ready |
| `quest_find_persi` | 3, 5 | Ready |
| `quest_garfield_comfort` | 3, 5 | Ready |
| `quest_sam_trust` | 3 | Ready |
| `quest_save_lada` | 3 | Ready |
| `quest_separation` | 1, 2 | Ready |
| `quest_voland_visit` | 3 | Ready |
| `quest_wedding` | 2 | Ready |
| `quest_work_deadline` | 1 | Ready |

### Конфликты

| Артефакт | Арка | Статус |
|---------|------|---------|
| `burnout` | 1, 5 | Ready |
| `father_concern` | 4 | Ready |
| `father_criticism` | 4 | Ready |
| `father_neglected` | 4 | Ready |
| `financial_disagreement` | 2 | Ready |
| `guilt` | 1, 4, 5 | Ready |
| `household_duties` | 2 | Ready |
| `identity_crisis` | 5 | Ready |
| `lack_of_attention` | 2 | Ready |
| `romantic_crisis` | 2 | Ready |

### NPC

| Артефакт | Арка | Статус |
|---------|------|---------|
| `alexander` | 1, 2, 5 | Ready |
| `aijan` | 3 | Ready |
| `cirilla` | 3 | Ready |
| `duke` | 3 | Ready |
| `garfield` | 3, 5 | Ready |
| `klop` | 3 | Ready |
| `lada` | 3 | Ready |
| `persi` | 3, 5 | Ready |
| `sam` | 3 | Ready |
| `thelma` | 3 | Ready |
| `voland` | 3 | Ready |

### События (world-events)

| Артефакт | Арка | Статус |
|---------|------|---------|
| `event_aijan_hiss` | 3 | Ready |
| `event_alexander_code` | 1, 2 | Ready |
| `event_alexander_snore` | 2 | Ready |
| `event_call_yulia` | 2, 4 | Ready |
| `event_cirilla_muselet` | 3 | Ready |
| `event_duke_chew` | 3 | Ready |
| `event_garfield_purr` | 3, 5 | Ready |
| `event_klop_belly` | 3 | Ready |
| `event_night_anxiety` | 1, 5 | Ready |
| `event_old_photo` | 2, 4, 5 | Ready |
| `event_persi_wisdom` | 3, 5 | Ready |
| `event_rain_outside` | 1, 5 | Ready |
| `event_sam_follows_tanya` | 3 | Ready |
| `event_thelma_zoomies` | 3 | Ready |
| `event_voland_memory` | 3 | Ready |

### Действия игрока

| Артефакт | Арка | Статус |
|---------|------|---------|
| `beauty_routine` | 5 | Ready |
| `call_husband` | 2 | Ready |
| `cook_food` | 2, 4 | Ready |
| `date_with_husband` | 2 | Ready |
| `eat_food` | 4, 5 | Ready |
| `feed_pets` | 3, 5 | Ready |
| `go_to_work` | 1 | Ready |
| `household` | 2 | Ready |
| `play_with_cat` | 3 | Ready |
| `rest_at_home` | 1, 5 | Ready |
| `self_care` | 5 | Ready |
| `visit_father` | 4 | Ready |
| `walk_dog` | 3 | Ready |
| `work_on_project` | 1 | Ready |

### Концовки

| Артефакт | Арка | Категория | Статус |
|---------|------|---------|--------|
| `balanced_life` | 5 | STORY | Ready |
| `bankruptcy` | 1, 2 | GAME_OVER | Ready |
| `burnout` (ending) | 1, 5 | GAME_OVER | Ready |
| `divorce` | 2 | GAME_OVER | Ready |
| `family_focus` | 2, 4 | STORY | Ready |
| `family_happiness` | 2 | STORY | Ready |
| `good_career` | 1 | STORY | Ready |
| `isolation` | 5 | GAME_OVER | Ready |
| `neutral_epilogue` | 3 | STORY | Ready |
| `workaholic_burnout` | 1 | GAME_OVER | Ready |

### Мир (документы world)

| Артефакт | Тип | Статус |
|---------|------|---------|
| `narrative/world/tatyana-facts.md` | world-bible | Ready |

---

## 6. Связные документы

- **World bible:** `narrative/world/tatyana-facts.md` (TASK-NA-010)
- **Спецификации NPC:** `narrative/npc-behavior/*.xml`
- **Спецификации квестов:** `narrative/quests/*.xml`
- **Спецификации конфликтов:** `narrative/confilcts/*.xml`
- **Спецификации событий:** `narrative/events/*.xml`
- **Действия игрока:** `narrative/player-actions/*.xml`
- **Концовки:** `narrative/endings/*.xml`

---

*Документ создан в рамках TASK-NA-011. Нарратор — Life of T.*
*Последнее обновление: 2026-03-30*
