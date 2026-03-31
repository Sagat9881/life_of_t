# ADR-003 — Выбор инструмента публикации на GitHub Pages

**Дата:** 2026-03-31  
**Статус:** Принято  
**Автор:** System Analyst  
**Связан с:** SPEC-SA-013, TASK-BE-022

---

## Контекст

Для публикации Project Status Dashboard на GitHub Pages необходимо выбрать Action.
Рассматривались два варианта:

1. **`peaceiris/actions-gh-pages@v3`** — сторонний экшен, публикует в ветку `gh-pages`.
2. **Официальный стек Pages** (`actions/configure-pages` + `actions/upload-pages-artifact` + `actions/deploy-pages@v4`).

---

## Варианты

### Вариант A: `peaceiris/actions-gh-pages@v3`

**Плюсы:**
- Прост в настройке (один step).
- Хорошо документирован, широко используется.

**Минусы:**
- Требует Personal Access Token (PAT) или Deploy Key — дополнительный секрет в репозитории.
- Сторонний экшен — риск депрекации или supply-chain атаки.
- Не поддерживает `environment: github-pages` и OIDC natively.
- Pages Source в настройках репозитория должен быть `gh-pages branch` (ручная настройка).

### Вариант B: Официальный стек GitHub Pages Actions

**Плюсы:**
- Поддерживается GitHub официально — гарантия совместимости.
- Использует OIDC-токены (`id-token: write`) — **не требует PAT**.
- Интеграция с `environment: github-pages` — статус деплоя виден в UI репозитория.
- Pages Source = «GitHub Actions» — чище, без служебной ветки `gh-pages`.
- Бесплатные minutes (только `ubuntu-latest`).

**Минусы:**
- Три отдельных step вместо одного.
- Требует явного указания `permissions: pages: write` в workflow.

---

## Решение

**Принят Вариант B** — официальный стек (`configure-pages` + `upload-pages-artifact` + `deploy-pages@v4`).

### Обоснование

1. Безопасность: нет необходимости хранить PAT в Secrets — используются OIDC-токены.
2. Интеграция: видимость статуса деплоя в разделе Environments репозитория.
3. Сопровождаемость: официальный инструмент не будет внезапно депрекирован.
4. Соответствие ограничению промта: «не требует дополнительных платных credits» — оба варианта эквивалентны, но Вариант B не требует ни одного секрета.

---

## Последствия

- В Settings → Pages репозитория Source должен быть установлен в **«GitHub Actions»** (см. SPEC-SA-013, раздел 5.1).
- Ветка `gh-pages` **не создаётся и не используется**.
- Job `deploy` в workflow обязан указывать `environment: github-pages`.

---

## Дополнительное решение: Bash vs Python для скриптов сбора метрик

**Вопрос:** использовать Bash-скрипты или Python для `count-tasks.sh`, `count-backend.sh` и пр.

**Принято:** **Bash**, по следующим причинам:
- `ubuntu-latest` включает Bash 5+ без дополнительной установки.
- Операции (find, grep, wc, jq) нативны для shell — нет зависимостей.
- Python потребовал бы `setup-python` step и, возможно, `pip install`.
- Скрипты простые (подсчёт файлов + формирование JSON через `jq`).
- Java Developer владеет Bash в контексте Maven-сборки (`java-developer-skill.md`).

**Исключение:** если логика значительно усложнится (парсинг вложенного XML, комплексные вычисления) — допустимо перейти на Python-скрипт в рамках отдельного ADR.
