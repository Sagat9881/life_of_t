package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.event.game.*;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.*;

/**
 * Utility AI engine: evaluates all candidate actions for each NPC,
 * scores them, and returns the best one (if any passes threshold).
 */
public class NpcBehaviorEngine {
    private static final double INITIATION_THRESHOLD = 0.5;
    private final Map<NpcCode, List<NpcActionCandidate>> candidateRegistry;
    private final Set<String> firedToday = new HashSet<>();

    public NpcBehaviorEngine() {
        this.candidateRegistry = new EnumMap<>(NpcCode.class);
        registerHusbandCandidates();
        registerFatherCandidates();
    }

    public Optional<GameEvent> evaluate(NpcProfile profile, int currentDay, int currentHour) {
        List<NpcActionCandidate> candidates = candidateRegistry.getOrDefault(profile.code(), List.of());

        return candidates.stream()
            .map(c -> Map.entry(c, c.score(profile, currentDay, currentHour)))
            .filter(e -> e.getValue() > INITIATION_THRESHOLD)
            .filter(e -> !firedToday.contains(e.getKey().id()))
            .max(Comparator.comparingDouble(Map.Entry::getValue))
            .map(e -> {
                firedToday.add(e.getKey().id());
                return NpcInitiatedEvent.create(e.getKey(), profile, currentDay);
            });
    }

    public void resetDaily() {
        firedToday.clear();
    }

    // ========== HUSBAND CANDIDATES ==========
    private void registerHusbandCandidates() {
        List<NpcActionCandidate> list = new ArrayList<>();

        list.add(new NpcActionCandidate("dinner_invite", "Let's have dinner?",
            "Sam suggests going out for dinner together.",
            GameEventType.RELATIONSHIP_MILESTONE,
            List.of(
                new EventOption("accept", "Sure, let's go!", "Wonderful evening together",
                    new StatChanges(0, 0, -10, 15, -25, 0), Map.of("HUSBAND", 10)),
                new EventOption("later", "Maybe another time", "Sam hides disappointment",
                    StatChanges.none(), Map.of("HUSBAND", -5)),
                new EventOption("home_cook", "Let's cook at home!", "Cozy and cheap",
                    new StatChanges(-5, 0, -8, 12, 0, 0), Map.of("HUSBAND", 8))
            ),
            0.6, (p, day, hour) -> p.mood().loneliness() >= 40 && p.schedule().isAvailable(hour)
        ));

        list.add(new NpcActionCandidate("work_concern", "You work too much...",
            "Sam expresses concern about your work-life balance.",
            GameEventType.RELATIONSHIP_MILESTONE,
            List.of(
                new EventOption("listen", "You're right, I'll slow down", "Sam feels heard",
                    new StatChanges(0, 0, -5, 5, 0, 5), Map.of("HUSBAND", 10)),
                new EventOption("dismiss", "I have to, we need money", "Tension rises",
                    new StatChanges(0, 0, 5, -5, 0, 0), Map.of("HUSBAND", -8)),
                new EventOption("discuss", "Let's talk about it", "Open conversation",
                    new StatChanges(0, 0, -3, 3, 0, 3), Map.of("HUSBAND", 5))
            ),
            0.7, (p, day, hour) -> p.memory().detectWorkObsession(5) && p.schedule().isAvailable(hour)
        ));

        list.add(new NpcActionCandidate("miss_you", "I miss you...",
            "Sam says he barely sees you lately.",
            GameEventType.RELATIONSHIP_MILESTONE,
            List.of(
                new EventOption("hug", "I miss you too", "Warm moment",
                    new StatChanges(0, 0, -5, 10, 0, 5), Map.of("HUSBAND", 12)),
                new EventOption("guilt", "I know, I'm sorry...", "Guilt lingers",
                    new StatChanges(0, 0, 5, -3, 0, -3), Map.of("HUSBAND", 3))
            ),
            0.8, (p, day, hour) -> p.mood().loneliness() >= 60
                && p.memory().isBeingIgnored(Set.of("DATE_WITH_HUSBAND"), 5)
                && p.schedule().isAvailable(hour)
        ));

        list.add(new NpcActionCandidate("movie_night", "Movie tonight?",
            "Sam found a movie he thinks you'd both enjoy.",
            GameEventType.RANDOM_ENCOUNTER,
            List.of(
                new EventOption("yes", "Sounds great!", "Fun relaxing evening",
                    new StatChanges(5, 0, -10, 10, 0, 0), Map.of("HUSBAND", 7)),
                new EventOption("tired", "I'm too tired", "Sam watches alone",
                    new StatChanges(0, 0, 0, -3, 0, 0), Map.of("HUSBAND", -3))
            ),
            0.4, (p, day, hour) -> hour >= 19 && p.mood().happiness() >= 40 && p.schedule().isAvailable(hour)
        ));

        candidateRegistry.put(NpcCode.HUSBAND, list);
    }

    // ========== FATHER CANDIDATES ==========
    private void registerFatherCandidates() {
        List<NpcActionCandidate> list = new ArrayList<>();

        list.add(new NpcActionCandidate("phone_call", "Dad is calling",
            "Your father calls to check on you.",
            GameEventType.FAMILY_EVENT,
            List.of(
                new EventOption("answer", "Hi Dad!", "Nice chat, he's happy",
                    new StatChanges(0, 0, -3, 5, 0, 3), Map.of("FATHER", 8)),
                new EventOption("ignore", "I'll call back later", "Missed call guilt",
                    new StatChanges(0, 0, 3, -3, 0, -2), Map.of("FATHER", -5)),
                new EventOption("visit", "I'll come visit!", "He's thrilled",
                    new StatChanges(-5, 0, -5, 8, 0, 5), Map.of("FATHER", 15))
            ),
            0.6, (p, day, hour) -> p.mood().loneliness() >= 45
                && p.schedule().isAvailable(hour)
                && day % 3 == 0
        ));

        list.add(new NpcActionCandidate("health_worry", "Dad doesn't sound well",
            "Your father mentions he's feeling unwell.",
            GameEventType.CRISIS,
            List.of(
                new EventOption("visit_now", "I'm coming right now!", "You visit, he feels better",
                    new StatChanges(-15, 0, 10, -5, 0, 5), Map.of("FATHER", 15)),
                new EventOption("call_doc", "Have you called the doctor?", "Practical advice",
                    new StatChanges(0, 0, 5, -3, 0, 0), Map.of("FATHER", 3)),
                new EventOption("worry_later", "Take care, I'll check tomorrow", "Lingering worry",
                    new StatChanges(0, 0, 8, -5, 0, -3), Map.of("FATHER", -5))
            ),
            0.9, (p, day, hour) -> day >= 10 && p.mood().energy() <= 35 && p.schedule().isAvailable(hour)
        ));

        list.add(new NpcActionCandidate("life_advice", "Dad wants to talk about life",
            "Your father wants to share some wisdom with you.",
            GameEventType.PERSONAL_GROWTH,
            List.of(
                new EventOption("listen", "I'd love to hear", "Touching conversation",
                    new StatChanges(0, 0, -5, 8, 0, 8), Map.of("FATHER", 10)),
                new EventOption("deflect", "Not now, Dad", "He's hurt",
                    new StatChanges(0, 0, 0, -3, 0, 0), Map.of("FATHER", -8))
            ),
            0.5, (p, day, hour) -> p.mood().happiness() >= 40
                && p.schedule().isAvailable(hour)
                && day >= 7
        ));

        list.add(new NpcActionCandidate("criticism", "Dad disapproves",
            "Your father criticizes your lifestyle choices.",
            GameEventType.FAMILY_EVENT,
            List.of(
                new EventOption("accept", "Maybe you're right...", "Self-doubt but peace",
                    new StatChanges(0, 0, 5, -5, 0, -5), Map.of("FATHER", 5)),
                new EventOption("argue", "It's my life!", "Heated argument",
                    new StatChanges(0, 0, 15, -10, 0, 5), Map.of("FATHER", -12)),
                new EventOption("calm", "I hear you, but I disagree", "Mature boundary",
                    new StatChanges(0, 0, 3, 0, 0, 8), Map.of("FATHER", -2))
            ),
            0.7, (p, day, hour) -> p.mood().irritation() >= 50 && p.schedule().isAvailable(hour)
        ));

        candidateRegistry.put(NpcCode.FATHER, list);
    }
}