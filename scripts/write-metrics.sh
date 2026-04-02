#!/usr/bin/env bash
# §4.1 — Assemble metrics.json from environment variables
set -euo pipefail

OUTPUT_FILE="${OUTPUT_FILE:-metrics.json}"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

jq -n \
  --arg timestamp        "$TIMESTAMP" \
  --argjson tasks_todo         "${TASKS_TODO:-0}" \
  --argjson tasks_in_progress  "${TASKS_IN_PROGRESS:-0}" \
  --argjson tasks_review       "${TASKS_REVIEW:-0}" \
  --argjson tasks_done         "${TASKS_DONE:-0}" \
  --argjson tasks_blocked      "${TASKS_BLOCKED:-0}" \
  --argjson tasks_total        "${TASKS_TOTAL:-0}" \
  --argjson backend_domain         "${BACKEND_DOMAIN:-0}" \
  --argjson backend_application    "${BACKEND_APPLICATION:-0}" \
  --argjson backend_infrastructure "${BACKEND_INFRASTRUCTURE:-0}" \
  --argjson backend_presentation   "${BACKEND_PRESENTATION:-0}" \
  --argjson backend_total          "${BACKEND_TOTAL:-0}" \
  --argjson narrative_quests       "${NARRATIVE_QUESTS:-0}" \
  --argjson narrative_npcs         "${NARRATIVE_NPCS:-0}" \
  --argjson narrative_world_events "${NARRATIVE_WORLD_EVENTS:-0}" \
  --argjson narrative_conflicts    "${NARRATIVE_CONFLICTS:-0}" \
  --argjson narrative_total        "${NARRATIVE_TOTAL:-0}" \
  --arg ci_status     "${CI_STATUS:-unknown}" \
  --arg ci_conclusion "${CI_CONCLUSION:-unknown}" \
  --argjson ci_total_runs "${CI_TOTAL_RUNS:-0}" \
  '{
    "generated_at": $timestamp,
    "tasks": {
      "todo":        $tasks_todo,
      "in_progress": $tasks_in_progress,
      "review":      $tasks_review,
      "done":        $tasks_done,
      "blocked":     $tasks_blocked,
      "total":       $tasks_total
    },
    "backend": {
      "domain":         $backend_domain,
      "application":    $backend_application,
      "infrastructure": $backend_infrastructure,
      "presentation":   $backend_presentation,
      "total":          $backend_total
    },
    "narrative": {
      "quests":       $narrative_quests,
      "npcs":         $narrative_npcs,
      "world_events": $narrative_world_events,
      "conflicts":    $narrative_conflicts,
      "total":        $narrative_total
    },
    "ci": {
      "status":     $ci_status,
      "conclusion": $ci_conclusion,
      "total_runs":  $ci_total_runs
    }
  }' > "$OUTPUT_FILE"

echo "metrics.json written to ${OUTPUT_FILE}"
