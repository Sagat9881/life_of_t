package ru.lifegame.backend.domain.event.game;

import ru.lifegame.backend.domain.model.session.GameSessionContext;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.*;

public class GameEventGenerator {

    private static final Set<Integer> STORY_DAYS = Set.of(3, 5, 7, 10, 15, 20, 25);
    private final Set<String> firedEventIds = new HashSet<>();

    public Optional<GameEvent> checkForEvent(GameSessionContext context) {
        int day = context.time().day();
        if (STORY_DAYS.contains(day)) {
            Optional<GameEvent> story = generateStoryEvent(day);
            if (story.isPresent() && !firedEventIds.contains(story.get().id())) {
                story.get().markTriggered();
                firedEventIds.add(story.get().id());
                return story;
            }
        }
        return generateStateEvent(context);
    }

    private Optional<GameEvent> generateStoryEvent(int day) {
        return switch (day) {
            case 3 -> Optional.of(evt("event_day3_dinner", GameEventType.RELATIONSHIP_MILESTONE,
                "Husband suggests dinner", "Sam: Let's go out for dinner?", day, List.of(
                    opt("accept", "Sure!", "Nice evening", new StatChanges(0,0,-5,15,-30,0), Map.of("HUSBAND",10)),
                    opt("postpone", "Maybe later", "Sam is a bit sad", new StatChanges(0,0,0,-5,0,0), Map.of("HUSBAND",-5)),
                    opt("home", "Order in", "Cozy evening", new StatChanges(0,0,-3,10,-15,0), Map.of("HUSBAND",5))
                )));
            case 5 -> Optional.of(evt("event_day5_father", GameEventType.FAMILY_EVENT,
                "Father calls", "Dad: How are you?", day, List.of(
                    opt("visit", "I'll come!", "Father is happy", StatChanges.none(), Map.of("FATHER",10)),
                    opt("busy", "I'm busy", "Father is sad", new StatChanges(0,0,5,-5,0,0), Map.of("FATHER",-10))
                )));
            case 7 -> Optional.of(evt("event_day7_project", GameEventType.WORK_OPPORTUNITY,
                "New work project", "Boss offers a complex project", day, List.of(
                    opt("take", "I'll do it!", "Challenging but profitable", new StatChanges(-10,0,15,0,100,0), Map.of()),
                    opt("decline", "No thanks", "Calm week ahead", new StatChanges(0,0,-5,5,0,0), Map.of()),
                    opt("negotiate", "With conditions", "Negotiated well", new StatChanges(-5,0,10,0,70,0), Map.of())
                )));
            case 15 -> Optional.of(evt("event_day15_reflect", GameEventType.PERSONAL_GROWTH,
                "Midgame reflection", "Half the month is gone", day, List.of(
                    opt("ok", "Doing well", "Positive outlook", new StatChanges(0,0,-10,10,0,5), Map.of()),
                    opt("bad", "Something's off", "Anxiety rises", new StatChanges(0,0,10,-10,0,-5), Map.of())
                )));
            case 20 -> Optional.of(evt("event_day20_cat", GameEventType.CRISIS,
                "Barsik is sick", "Barsik looks weak", day, List.of(
                    opt("vet", "Go to vet!", "Vet says it's ok", new StatChanges(-10,0,10,-5,-50,0), Map.of()),
                    opt("wait", "Wait and see", "Barsik not better", new StatChanges(0,0,15,-10,0,0), Map.of())
                )));
            default -> Optional.empty();
        };
    }

    private Optional<GameEvent> generateStateEvent(GameSessionContext context) {
        int stress = context.player().stats().stress();
        int money = context.player().stats().money();
        int day = context.time().day();

        if (stress > 70 && !firedEventIds.contains("stress_" + day)) {
            var e = evt("stress_" + day, GameEventType.CRISIS, "Stress overload", "Headache", day, List.of(
                opt("rest", "Cancel plans", "Relief", new StatChanges(20,0,-20,10,0,0), Map.of()),
                opt("push", "Push through", "Body at limit", new StatChanges(0,-5,5,-5,0,0), Map.of())
            ));
            e.markTriggered(); firedEventIds.add(e.id());
            return Optional.of(e);
        }
        if (money < 200 && !firedEventIds.contains("money_" + day)) {
            var e = evt("money_" + day, GameEventType.CRISIS, "Low money", "Almost broke", day, List.of(
                opt("work", "Extra work", "Hard but needed", new StatChanges(-20,0,15,-10,80,0), Map.of()),
                opt("father", "Ask father", "He helps reluctantly", new StatChanges(0,0,5,-5,50,0), Map.of("FATHER",-5)),
                opt("cut", "Cut expenses", "Saving mode", new StatChanges(0,0,5,-5,20,0), Map.of())
            ));
            e.markTriggered(); firedEventIds.add(e.id());
            return Optional.of(e);
        }
        return Optional.empty();
    }

    private static GameEvent evt(String id, GameEventType t, String title, String desc, int day, List<EventOption> opts) {
        return new GameEvent(id, t, title, desc, opts, Map.of(), 1, day);
    }

    private static EventOption opt(String id, String text, String result, StatChanges s, Map<String,Integer> r) {
        return new EventOption(id, text, result, s, r);
    }
}
