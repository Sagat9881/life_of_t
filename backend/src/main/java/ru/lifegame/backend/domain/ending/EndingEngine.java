package ru.lifegame.backend.domain.ending;

import ru.lifegame.backend.domain.ending.spec.EndingConditionEvaluator;
import ru.lifegame.backend.domain.ending.spec.EndingLoader;
import ru.lifegame.backend.domain.ending.spec.EndingSpec;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.quest.QuestLog;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Data-driven ending engine.
 * Loads endings from XML, evaluates conditions, selects best matching ending.
 */
public class EndingEngine {
    private final List<EndingSpec> storyEndings;
    private final List<EndingSpec> gameOverEndings;
    private final EndingConditionEvaluator evaluator;

    public EndingEngine(InputStream xmlStream) throws Exception {
        EndingLoader loader = new EndingLoader();
        List<EndingSpec> allEndings = loader.load(xmlStream);

        this.storyEndings = allEndings.stream()
            .filter(e -> "STORY".equals(e.category()))
            .sorted(Comparator.comparingInt(EndingSpec::priority).reversed())
            .collect(Collectors.toList());

        this.gameOverEndings = allEndings.stream()
            .filter(e -> "GAME_OVER".equals(e.category()))
            .sorted(Comparator.comparingInt(EndingSpec::priority).reversed())
            .collect(Collectors.toList());

        this.evaluator = new EndingConditionEvaluator();
    }

    /**
     * Find best story ending (evaluated on day 30).
     */
    public Optional<Ending> findBestStoryEnding(
        PlayerCharacter player,
        Relationships relationships,
        Pets pets,
        QuestLog questLog
    ) {
        return storyEndings.stream()
            .filter(spec -> evaluateConditions(spec, player, relationships, questLog))
            .findFirst()
            .map(this::toEnding);
    }

    /**
     * Check for game-over ending (evaluated every day).
     */
    public Optional<Ending> checkGameOver(
        PlayerCharacter player,
        Relationships relationships,
        Pets pets,
        QuestLog questLog
    ) {
        // Pet death check (not in XML yet, can add later)
        if (pets.hasDeadPet()) {
            return Optional.of(new Ending(
                "PET_DEATH",
                EndingCategory.GAME_OVER_ENDING,
                "Питомец погиб",
                "Нужно было заботиться о питомце.",
                "Попробуйте ещё раз."
            ));
        }

        return gameOverEndings.stream()
            .filter(spec -> evaluateConditions(spec, player, relationships, questLog))
            .findFirst()
            .map(this::toEnding);
    }

    private boolean evaluateConditions(
        EndingSpec spec,
        PlayerCharacter player,
        Relationships relationships,
        QuestLog questLog
    ) {
        EndingSpec.EndingConditions conditions = spec.conditions();
        List<EndingSpec.EndingCondition> condList = conditions.conditions();

        if (condList == null || condList.isEmpty()) {
            return true; // Always-true fallback (NEUTRAL_EPILOGUE)
        }

        boolean result = "OR".equals(conditions.mode())
            ? condList.stream().anyMatch(c -> evaluator.evaluate(c, player, relationships, questLog))
            : condList.stream().allMatch(c -> evaluator.evaluate(c, player, relationships, questLog));

        return result;
    }

    private Ending toEnding(EndingSpec spec) {
        EndingCategory category = "STORY".equals(spec.category())
            ? EndingCategory.STORY_ENDING
            : EndingCategory.GAME_OVER_ENDING;

        return new Ending(
            spec.id(),
            category,
            spec.meta().title(),
            spec.meta().summary(),
            spec.meta().epilogue()
        );
    }
}
