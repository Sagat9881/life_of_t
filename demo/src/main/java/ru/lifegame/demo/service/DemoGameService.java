package ru.lifegame.demo.service;

import org.springframework.stereotype.Service;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.character.JobInfo;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.character.Skills;
import ru.lifegame.backend.domain.model.stats.Stats;
import ru.lifegame.backend.domain.model.common.Location;
import ru.lifegame.backend.domain.model.common.PlayerId;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.quest.Quest;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestObjective;
import ru.lifegame.backend.domain.quest.QuestStepState;
import ru.lifegame.backend.domain.quest.QuestType;
import ru.lifegame.demo.dto.DemoDtos.CharacterDto;
import ru.lifegame.demo.dto.DemoDtos.GameStateDto;
import ru.lifegame.demo.dto.DemoDtos.GameTimeDto;
import ru.lifegame.demo.dto.DemoDtos.QuestSummaryDto;
import ru.lifegame.demo.dto.DemoDtos.RelationshipDto;
import ru.lifegame.demo.dto.DemoDtos.RelationshipsDto;
import ru.lifegame.demo.dto.DemoDtos.StatsDto;
import ru.lifegame.demo.dto.DemoDtos.StepDto;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manages the single demo game session.
 *
 * <p>Creates real domain objects on construction and exposes DTO projections for
 * the REST layer. Thread-safety is provided via {@link AtomicReference} wrappers
 * so the Spring singleton can be read concurrently by HTTP threads.</p>
 *
 * <p>Package: {@code ru.lifegame.demo.service}</p>
 */
@Service
public class DemoGameService {

    // -----------------------------------------------------------------------
    // Mutable game state (replaced atomically on mutations)
    // -----------------------------------------------------------------------

    private final AtomicReference<PlayerCharacter> character = new AtomicReference<>();
    private final AtomicReference<Relationships>   relationships = new AtomicReference<>();
    private final AtomicReference<QuestLog>        questLog = new AtomicReference<>();
    private final AtomicReference<GameTime>        gameTime = new AtomicReference<>();

    public DemoGameService() {
        reset();
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    /**
     * (Re)creates the demo session with fresh domain objects.
     * Called from constructor and can be called from tests.
     */
    public void reset() {
        character.set(buildTanya());
        relationships.set(buildInitialRelationships());
        questLog.set(buildInitialQuestLog());
        gameTime.set(GameTime.initial());
    }

    // -----------------------------------------------------------------------
    // Accessors for direct domain-object access (used by tests)
    // -----------------------------------------------------------------------

    public PlayerCharacter getCharacter()      { return character.get(); }
    public Relationships   getRelationships()  { return relationships.get(); }
    public QuestLog        getQuestLog()       { return questLog.get(); }
    public GameTime        getGameTime()       { return gameTime.get(); }

    public void setCharacter(PlayerCharacter pc)  { character.set(pc); }
    public void setRelationships(Relationships r) { relationships.set(r); }
    public void setQuestLog(QuestLog ql)          { questLog.set(ql); }

    // -----------------------------------------------------------------------
    // DTO builders
    // -----------------------------------------------------------------------

    public GameStateDto buildGameStateDto() {
        return new GameStateDto(
                buildCharacterDto(),
                buildRelationshipsDto(),
                buildGameTimeDto(),
                buildActiveQuestDtos(),
                List.of()  // asset list populated by AssetController
        );
    }

    public CharacterDto buildCharacterDto() {
        PlayerCharacter pc = character.get();
        Stats s = pc.stats();
        return new CharacterDto(
                pc.name(),
                new StatsDto(s.energy(), s.health(), s.stress(), s.mood(), s.money(), s.selfEsteem()),
                pc.job().title(),
                pc.location().name(),
                pc.isBurnedOut(),
                pc.isInInternalCrisis(),
                pc.isBankrupt()
        );
    }

    public RelationshipsDto buildRelationshipsDto() {
        Relationships rels = relationships.get();
        Map<String, RelationshipDto> byNpc = new java.util.LinkedHashMap<>();
        for (NpcCode code : NpcCode.values()) {
            Relationship r = rels.get(code);
            if (r != null) {
                byNpc.put(code.name(), new RelationshipDto(
                        code.name(),
                        r.closeness(),
                        r.trust(),
                        r.stability(),
                        r.romance(),
                        r.broken()
                ));
            }
        }
        return new RelationshipsDto(byNpc);
    }

    public GameTimeDto buildGameTimeDto() {
        GameTime gt = gameTime.get();
        return new GameTimeDto(gt.day(), gt.hour());
    }

    public List<QuestSummaryDto> buildActiveQuestDtos() {
        return questLog.get().activeQuests().stream()
                .map(this::toQuestSummaryDto)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private QuestSummaryDto toQuestSummaryDto(Quest q) {
        List<StepDto> steps = q.steps().stream()
                .map(s -> new StepDto(
                        s.objective().description(),
                        s.objective().required(),
                        s.currentCount(),
                        s.isCompleted()
                ))
                .collect(Collectors.toList());

        return new QuestSummaryDto(
                q.id(),
                q.type().name(),
                q.title(),
                q.isCompleted() ? "COMPLETED" : (q.isActive() ? "IN_PROGRESS" : "NOT_STARTED"),
                q.progressPercent(),
                steps
        );
    }

    // -----------------------------------------------------------------------
    // Domain-object factories
    // -----------------------------------------------------------------------

    private static PlayerCharacter buildTanya() {
        return new PlayerCharacter(
                new PlayerId("tanya-001"),
                "Tanya",
                Stats.initial(),
                JobInfo.initial(),
                Location.HOME,
                Map.of(),
                Skills.initial(),
                List.of()
        );
    }

    private static Relationships buildInitialRelationships() {
        Map<NpcCode, Relationship> map = new EnumMap<>(NpcCode.class);
        map.put(NpcCode.HUSBAND, new Relationship(NpcCode.HUSBAND, 65, 60, 55, 70, 1, false));
        map.put(NpcCode.FATHER,  new Relationship(NpcCode.FATHER,  50, 55, 60,  0, 1, false));
        return new Relationships(map);
    }

    private static QuestLog buildInitialQuestLog() {
        QuestLog log = new QuestLog();

        // SELF_CARE_ARC quest – two steps
        Quest selfCare = new Quest(
                "quest-self-care-01",
                QuestType.SELF_CARE_ARC,
                "Найди себя",
                "Позаботься о себе: отдохни и займись спортом",
                List.of(
                        new QuestStepState(new QuestObjective("Поспать не менее 7 часов", 1), 0),
                        new QuestStepState(new QuestObjective("Сходить в спортзал", 1), 0)
                )
        );
        selfCare.start();
        log.addQuest(selfCare);

        // FAMILY_HARMONY quest – two steps
        Quest family = new Quest(
                "quest-family-01",
                QuestType.FAMILY_HARMONY,
                "Семейный вечер",
                "Укрепи отношения с мужем и папой",
                List.of(
                        new QuestStepState(new QuestObjective("Поговорить с мужем", 1), 0),
                        new QuestStepState(new QuestObjective("Позвонить папе", 1), 0)
                )
        );
        family.start();
        log.addQuest(family);

        // CAREER_GROWTH quest – one step (not started yet, unlocked after family)
        Quest career = new Quest(
                "quest-career-01",
                QuestType.CAREER_GROWTH,
                "Карьерный рост",
                "Выполни рабочий проект и получи признание",
                List.of(
                        new QuestStepState(new QuestObjective("Завершить проект", 3), 0)
                )
        );
        log.addQuest(career);

        return log;
    }
}
