#!/usr/bin/env bash
# §4.1 — Assemble metrics.json from environment variables
set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/lib.sh"

OUTPUT_FILE="${OUTPUT_FILE:-metrics.json}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Trim full SHA to 8 chars for short_sha
GIT_SHORT_SHA_VAL="${GIT_SHORT_SHA:-${GIT_SHA:-}}"
GIT_SHORT_SHA_VAL="${GIT_SHORT_SHA_VAL:0:8}"

jq -n \
  --arg  timestamp                   "$TIMESTAMP" \
  --arg  git_sha                     "${GIT_SHA:-}" \
  --arg  git_short_sha               "$GIT_SHORT_SHA_VAL" \
  --arg  git_branch                  "${GIT_BRANCH:-}" \
  --argjson tasks_in_progress        "$(val_int "${TASKS_IN_PROGRESS:-}")" \
  --argjson tasks_done               "$(val_int "${TASKS_DONE:-}")" \
  --argjson tasks_total              "$(val_int "${TASKS_TOTAL:-}")" \
  --argjson tasks_backend            "$(val_int "${TASKS_BACKEND:-}")" \
  --argjson tasks_narrative          "$(val_int "${TASKS_NARRATIVE:-}")" \
  --argjson tasks_frontend           "$(val_int "${TASKS_FRONTEND:-}")" \
  --argjson tasks_assets             "$(val_int "${TASKS_ASSETS:-}")" \
  --argjson tasks_analytics          "$(val_int "${TASKS_ANALYTICS:-}")" \
  --argjson backend_domain           "$(val_int "${BACKEND_DOMAIN:-}")" \
  --argjson backend_application      "$(val_int "${BACKEND_APPLICATION:-}")" \
  --argjson backend_infrastructure   "$(val_int "${BACKEND_INFRASTRUCTURE:-}")" \
  --argjson backend_presentation     "$(val_int "${BACKEND_PRESENTATION:-}")" \
  --argjson backend_total            "$(val_int "${BACKEND_TOTAL:-}")" \
  --argjson backend_test_total       "$(val_int "${BACKEND_TEST_TOTAL:-}")" \
  --argjson asset_generator_total    "$(val_int "${ASSET_GENERATOR_TOTAL:-}")" \
  --argjson narrative_quests         "$(val_int "${NARRATIVE_QUESTS:-}")" \
  --argjson narrative_npcs           "$(val_int "${NARRATIVE_NPCS:-}")" \
  --argjson narrative_world_events   "$(val_int "${NARRATIVE_WORLD_EVENTS:-}")" \
  --argjson narrative_conflicts      "$(val_int "${NARRATIVE_CONFLICTS:-}")" \
  --argjson narrative_world          "$(val_int "${NARRATIVE_WORLD:-}")" \
  --argjson narrative_endings        "$(val_int "${NARRATIVE_ENDINGS:-}")" \
  --argjson narrative_player_actions "$(val_int "${NARRATIVE_PLAYER_ACTIONS:-}")" \
  --argjson narrative_reviews        "$(val_int "${NARRATIVE_REVIEWS:-}")" \
  --argjson narrative_asset_specs    "$(val_int "${NARRATIVE_ASSET_SPECS:-}")" \
  --argjson narrative_total          "$(val_int "${NARRATIVE_TOTAL:-}")" \
  --arg  ci_status                   "${CI_STATUS:-unknown}" \
  --arg  ci_conclusion               "${CI_CONCLUSION:-unknown}" \
  --argjson ci_total_runs            "$(val_int "${CI_TOTAL_RUNS:-}")" \
  --argjson ci_failed_checks         "$(val_int "${CI_FAILED_CHECKS:-}")" \
  --argjson ci_success_checks        "$(val_int "${CI_SUCCESS_CHECKS:-}")" \
  '{
    "generated_at": $timestamp,
    "git": {
      "sha":       $git_sha,
      "short_sha": $git_short_sha,
      "branch":    $git_branch
    },
    "tasks": {
      "in_progress": $tasks_in_progress,
      "done":        $tasks_done,
      "total":       $tasks_total
    },
    "tasks_by_category": {
      "backend":   $tasks_backend,
      "narrative": $tasks_narrative,
      "frontend":  $tasks_frontend,
      "assets":    $tasks_assets,
      "analytics": $tasks_analytics
    },
    "backend": {
      "domain":              $backend_domain,
      "application":         $backend_application,
      "infrastructure":      $backend_infrastructure,
      "presentation":        $backend_presentation,
      "total":               $backend_total,
      "test_total":          $backend_test_total,
      "asset_generator":     $asset_generator_total
    },
    "narrative": {
      "quests":         $narrative_quests,
      "npcs":           $narrative_npcs,
      "world_events":   $narrative_world_events,
      "conflicts":      $narrative_conflicts,
      "world":          $narrative_world,
      "endings":        $narrative_endings,
      "player_actions": $narrative_player_actions,
      "reviews":        $narrative_reviews,
      "asset_specs":    $narrative_asset_specs,
      "total":          $narrative_total
    },
    "ci": {
      "status":         $ci_status,
      "conclusion":     $ci_conclusion,
      "total_runs":     $ci_total_runs,
      "failed_checks":  $ci_failed_checks,
      "success_checks": $ci_success_checks
    }
  }' > "$OUTPUT_FILE"

log_info "metrics.json written to ${OUTPUT_FILE}"
