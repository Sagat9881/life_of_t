package ru.lifegame.backend.domain.narrative;

import ru.lifegame.backend.domain.event.game.EventOption;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec.EffectSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec.OptionSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a parsed {@link EventSpec} (data from XML) into a runtime
 * {@link GameEvent} that can be attached to a {@link ru.lifegame.backend.domain.model.session.GameSession}.
 *
 * Key responsibility: translate each {@link OptionSpec}'s {@link EffectSpec} list
 * into typed {@link StatChanges} and {@code Map<npcId, delta>} so that
 * {@link ru.lifegame.backend.domain.model.session.GameSession#chooseEventOption}
 * can apply them to the player character and relationships.
 *
 * Supported effect types (from EventSpec XML):
 *   stat-change          — target is a stat name, value is a signed int string
 *   relationship-change  — target is an NPC id,   value is a signed int string
 *
 * Supported stat targets (case-insensitive):
 *   energy, health, stress, mood, money, selfEsteem / self-esteem / self_esteem
 */
public final class EventSpecMapper {

    private EventSpecMapper() {}

    /**
     * Convert a spec + current day into a runtime GameEvent.
     * The returned event is NOT yet triggered — caller must call
     * {@link ru.lifegame.backend.domain.model.session.GameSession#triggerEvent(GameEvent)}.
     */
    public static GameEvent toGameEvent(EventSpec spec, int currentDay) {
        List<EventOption> options = spec.options().stream()
                .map(EventSpecMapper::toEventOption)
                .toList();

        List<GameEvent.DialogueLine> dialogue = spec.dialogue().stream()
                .map(d -> new GameEvent.DialogueLine(d.speaker(), d.textRu()))
                .toList();

        return new GameEvent(
                spec.id(),
                spec.meta().type(),
                spec.meta().titleRu(),
                spec.meta().descriptionRu(),
                dialogue,
                options,
                Map.of(),
                0,
                currentDay
        );
    }

    // ── option conversion ─────────────────────────────────────────────────────

    private static EventOption toEventOption(OptionSpec optionSpec) {
        StatChanges statChanges = buildStatChanges(optionSpec.effects());
        Map<String, Integer> relChanges = buildRelationshipChanges(optionSpec.effects());
        return new EventOption(optionSpec.id(), optionSpec.labelRu(), statChanges, relChanges);
    }

    // ── effect parsing ──────────────────────────────────────────────────────

    private static StatChanges buildStatChanges(List<EffectSpec> effects) {
        int energy = 0, health = 0, stress = 0, mood = 0, money = 0, selfEsteem = 0;
        for (EffectSpec e : effects) {
            if (!"stat-change".equals(e.type())) continue;
            int v = e.intValue();
            switch (normaliseStat(e.target())) {
                case "energy"     -> energy     += v;
                case "health"     -> health     += v;
                case "stress"     -> stress      += v;
                case "mood"       -> mood        += v;
                case "money"      -> money       += v;
                case "selfesteem" -> selfEsteem  += v;
                default -> System.err.println(
                        "[EventSpecMapper] Unknown stat target: '" + e.target() + "' — ignored");
            }
        }
        return new StatChanges(energy, health, stress, mood, money, selfEsteem);
    }

    private static Map<String, Integer> buildRelationshipChanges(List<EffectSpec> effects) {
        Map<String, Integer> result = new HashMap<>();
        for (EffectSpec e : effects) {
            if (!"relationship-change".equals(e.type())) continue;
            result.merge(e.target(), e.intValue(), Integer::sum);
        }
        return result.isEmpty() ? Map.of() : Map.copyOf(result);
    }

    /** Normalise stat names so XML variants map to the same key. */
    private static String normaliseStat(String raw) {
        if (raw == null) return "";
        return raw.toLowerCase()
                .replace("-", "")
                .replace("_", "");
    }
}
