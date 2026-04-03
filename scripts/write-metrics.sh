#!/usr/bin/env bash
# §4.1 — Assemble metrics.json from environment variables
set -euo pipefail

OUTPUT_FILE="${OUTPUT_FILE:-metrics.json}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# ─── Validate integer input (guard against empty strings breaking jq) ───
val_int() { local v="${1:-0}"; [[ "$v" =~ ^[0-9]+$ ]] && echo "$v" || echo "0"; }

jq -n \
  --arg  timestamp                  "$TIMESTAMP" \
  --arg  git_sha                    "${GIT_SHA:-}" \
  --arg  git_short_sha              "${GIT_SHORT_SHA:-}" \
  --arg  git_branch                 "${GIT_BRANCH:-}" \
  --argjson tasks_in_progress       "$(val_int "${TASKS_IN_PROGRESS:-}")" \
  --argjson tasks_done              "$(val_int "${TASKS_DONE:-}")" \
  --argjson tasks_total             "$(val_int "${TASKS_TOTAL:-}")" \
  --argjson tasks_backend           "$(val_int "${TASKS_BACKEND:-}")" \
  --argjson tasks_narrative         "$(val_int "${TASKS_NARRATIVE:-}")" \
  --argjson tasks_frontend          "$(val_int "${TASKS_FRONTEND:-}")" \
  --argjson tasks_assets            "$(val_int "${TASKS_ASSETS:-}")" \
  --argjson tasks_analytics         "$(val_int "${TASKS_ANALYTICS:-}")" \
  --argjson backend_domain          "$(val_int "${BACKEND_DOMAIN:-}")" \
  --argjson backend_application     "$(val_int "${BACKEND_APPLICATION:-}")" \
  --argjson backend_infrastructure  "$(val_int "${BACKEND_INFRASTRUCTURE:-}")" \
  --argjson backend_presentation    "$(val_int "${BACKEND_PRESENTATION:-}")" \
  --argjson backend_total           "$(val_int "${BACKEND_TOTAL:-}")" \
  --argjson narrative_quests        "$(val_int "${NARRATIVE_QUESTS:-}")" \
  --argjson narrative_npcs          "$(val_int "${NARRATIVE_NPCS:-}")" \
  --argjson narrative_world_events  "$(val_int "${NARRATIVE_WORLD_EVENTS:-}")" \
  --argjson narrative_conflicts     "$(val_int "${NARRATIVE_CONFLICTS:-}")" \
  --argjson narrative_world         "$(val_int "${NARRATIVE_WORLD:-}")" \
  --argjson narrative_endings       "$(val_int "${NARRATIVE_ENDINGS:-}")" \
  --argjson narrative_player_actions "$(val_int "${NARRATIVE_PLAYER_ACTIONS:-}")" \
  --argjson narrative_reviews       "$(val_int "${NARRATIVE_REVIEWS:-}")" \
  --argjson narrative_asset_specs   "$(val_int "${NARRATIVE_ASSET_SPECS:-}")" \
  --argjson narrative_total         "$(val_int "${NARRATIVE_TOTAL:-}")" \
  --arg  ci_status                  "${CI_STATUS:-unknown}" \
  --arg  ci_conclusion              "${CI_CONCLUSION:-unknown}" \
  --argjson ci_total_runs           "$(val_int "${CI_TOTAL_RUNS:-}")" \
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
      "domain":         $backend_domain,
      "application":    $backend_application,
      "infrastructure": $backend_infrastructure,
      "presentation":   $backend_presentation,
      "total":          $backend_total
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
      "status":     $ci_status,
      "conclusion": $ci_conclusion,
      "total_runs":  $ci_total_runs
    }
  }' > "$OUTPUT_FILE"

echo "metrics.json written to ${OUTPUT_FILE}"
