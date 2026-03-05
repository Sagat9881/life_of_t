package ru.lifegame.demo.service;

import org.springframework.stereotype.Service;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.character.JobInfo;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.character.Skills;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.model.stats.Stats;
import ru.lifegame.backend.domain.model.common.Location;
import ru.lifegame.backend.domain.model.common.PlayerId;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.quest.Quest;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestObjective;
import ru.lifegame.backend.domain.quest.QuestStepState;
import ru.lifegame.backend.domain.quest.QuestType;
import ru.lifegame.demo.dto.DemoDtos.ActionResultDto;
import ru.lifegame.demo.dto.DemoDtos.CharacterDto;
import ru.lifegame.demo.dto.DemoDtos.GameStateDto;
import ru.lifegame.demo.dto.DemoDtos.GameTimeDto;
import ru.lifegame.demo.dto.DemoDtos.QuestSummaryDto;
import ru.lifegame.demo.dto.DemoDtos.RelationshipDto;
import ru.lifegame.demo.dto.DemoDtos.RelationshipsDto;
import ru.lifegame.demo.dto.DemoDtos.StatsDto;
import ru.lifegame.demo.dto.DemoDtos.StepDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DemoGameService {

    private final AtomicReference<PlayerCharacter> character = new AtomicReference<>();
    private final AtomicReference<Relationships>   relationships = new AtomicReference<>();
    private final AtomicReference<QuestLog>        questLog = new AtomicReference<>();
    private final AtomicReference<GameTime>        gameTime = new AtomicReference<>();

    public DemoGameService() {
        reset();
    }

    public void reset() {
        character.set(buildTanya());
        relationships.set(buildInitialRelationships());
        questLog.set(buildInitialQuestLog());
        gameTime.set(GameTime.initial());
    }

    public PlayerCharacter getCharacter()      { return character.get(); }
    public Relationships   getRelationships()  { return relationships.get(); }
    public QuestLog        getQuestLog()       { return questLog.get(); }
    public GameTime        getGameTime()       { return gameTime.get(); }

    public void setCharacter(PlayerCharacter pc)  { character.set(pc); }
    public void setRelationships(Relationships r) { relationships.set(r); }
    public void setQuestLog(QuestLog ql)          { questLog.set(ql); }

    // =======================================================================
    // Action execution
    // =======================================================================

    public ActionResultDto executeAction(String target, String actionCode) {
        String key = (target + ":" + actionCode).toLowerCase();
        Map<String, Integer> statChanges = new LinkedHashMap<>();
        Map<String, Integer> relChanges = new LinkedHashMap<>();
        String narrative;
        int timeCost = 1;

        switch (key) {
            // --- Alexander interactions ---
            case "alexander:talk" -> {
                narrative = "Таня поговорила с Александром о планах на вечер. Он улыбнулся и предложил посмотреть фильм вместе.";
                statChanges.put("mood", 10);
                statChanges.put("stress", -5);
                statChanges.put("energy", -5);
                relChanges.put("closeness", 5);
                relChanges.put("trust", 3);
                timeCost = 1;
                advanceQuestStep("social", "husband");
            }
            case "alexander:hug" -> {
                narrative = "Таня обняла Александра. Он крепко обнял в ответ — на душе стало теплее.";
                statChanges.put("mood", 15);
                statChanges.put("stress", -10);
                statChanges.put("energy", -3);
                relChanges.put("closeness", 8);
                relChanges.put("romance", 10);
                timeCost = 1;
            }
            case "alexander:date" -> {
                narrative = "Таня и Александр устроили свидание — ужин при свечах, вино, разговоры до полуночи.";
                statChanges.put("mood", GameBalance.DATE_MOOD);
                statChanges.put("stress", GameBalance.DATE_STRESS);
                statChanges.put("energy", GameBalance.DATE_ENERGY);
                relChanges.put("closeness", GameBalance.DATE_CLOSENESS);
                relChanges.put("romance", GameBalance.DATE_ROMANCE);
                timeCost = GameBalance.DATE_HUSBAND_TIME_COST;
                advanceQuestStep("social", "husband");
            }
            // --- Sam (dog) interactions ---
            case "sam:pet" -> {
                narrative = "Таня погладила Сэма. Он завилял хвостом и подставил пузико.";
                statChanges.put("mood", 8);
                statChanges.put("stress", -5);
                statChanges.put("energy", -2);
                timeCost = 1;
            }
            case "sam:feed" -> {
                narrative = "Таня насыпала Сэму корм. Он радостно захрустел — миска опустела за минуту.";
                statChanges.put("mood", 5);
                statChanges.put("energy", -3);
                timeCost = 1;
            }
            case "sam:walk" -> {
                narrative = "Таня вышла с Сэмом на прогулку. Свежий воздух и весёлый пёс — лучшее лекарство от стресса.";
                statChanges.put("mood", GameBalance.WALK_DOG_MOOD);
                statChanges.put("stress", GameBalance.WALK_DOG_STRESS);
                statChanges.put("energy", GameBalance.WALK_DOG_ENERGY);
                statChanges.put("health", GameBalance.WALK_DOG_HEALTH);
                timeCost = GameBalance.WALK_DOG_TIME_COST;
            }
            // --- Self (Tanya) interactions ---
            case "tanya:rest" -> {
                narrative = "Таня прилегла отдохнуть. Сон был крепким и восстанавливающим.";
                statChanges.put("energy", GameBalance.REST_ENERGY);
                statChanges.put("stress", GameBalance.REST_STRESS);
                statChanges.put("mood", GameBalance.REST_MOOD);
                timeCost = GameBalance.REST_TIME_COST;
                advanceQuestStep("rest", "sleep");
            }
            case "tanya:selfcare" -> {
                narrative = "Таня приняла ванну с пеной, сделала маску — настроение заметно улучшилось.";
                statChanges.put("mood", GameBalance.SELF_CARE_MOOD);
                statChanges.put("stress", GameBalance.SELF_CARE_STRESS);
                statChanges.put("energy", GameBalance.SELF_CARE_ENERGY);
                statChanges.put("selfEsteem", GameBalance.SELF_CARE_SELF_ESTEEM);
                timeCost = GameBalance.SELF_CARE_TIME_COST;
            }
            case "tanya:callfather" -> {
                narrative = "Таня позвонила папе. Он рассказал о рыбалке и спросил, как дела на работе.";
                statChanges.put("mood", GameBalance.VISIT_FATHER_MOOD);
                statChanges.put("energy", -5);
                relChanges.put("father_closeness", GameBalance.VISIT_FATHER_CLOSENESS);
                relChanges.put("father_trust", GameBalance.VISIT_FATHER_TRUST);
                timeCost = 1;
                advanceQuestStep("social", "father");
            }
            default -> {
                return new ActionResultDto(
                        false, "Неизвестное действие: " + key, "",
                        Map.of(), Map.of(), buildGameStateDto()
                );
            }
        }

        // Apply stat changes via domain model
        applyStatChanges(statChanges);

        // Apply relationship changes
        applyRelationshipChanges(target, relChanges);

        // Advance time
        advanceTime(timeCost);

        return new ActionResultDto(
                true,
                "OK",
                narrative,
                statChanges,
                relChanges,
                buildGameStateDto()
        );
    }

    private void applyStatChanges(Map<String, Integer> changes) {
        PlayerCharacter pc = character.get();
        int energy = 0, health = 0, stress = 0, mood = 0, money = 0, selfEsteem = 0;

        for (var e : changes.entrySet()) {
            switch (e.getKey()) {
                case "energy"     -> energy     = e.getValue();
                case "health"     -> health     = e.getValue();
                case "stress"     -> stress     = e.getValue();
                case "mood"       -> mood       = e.getValue();
                case "money"      -> money      = e.getValue();
                case "selfEsteem" -> selfEsteem = e.getValue();
            }
        }

        pc.applyStatChanges(new StatChanges(energy, health, stress, mood, money, selfEsteem));
    }

    private void applyRelationshipChanges(String target, Map<String, Integer> changes) {
        if (changes.isEmpty()) return;
        Relationships rels = relationships.get();
        Map<NpcCode, Relationship> map = new EnumMap<>(NpcCode.class);
        for (NpcCode code : NpcCode.values()) {
            Relationship r = rels.get(code);
            if (r != null) map.put(code, r);
        }

        // Determine which NPC to modify
        boolean hasFatherChanges = changes.keySet().stream().anyMatch(k -> k.startsWith("father_"));

        if (hasFatherChanges && map.containsKey(NpcCode.FATHER)) {
            Relationship old = map.get(NpcCode.FATHER);
            int closeness = old.closeness();
            int trust = old.trust();
            int stability = old.stability();
            int romance = old.romance();
            for (var e : changes.entrySet()) {
                String k = e.getKey().replace("father_", "");
                switch (k) {
                    case "closeness" -> closeness = clamp(closeness + e.getValue());
                    case "trust"     -> trust     = clamp(trust + e.getValue());
                    case "stability" -> stability = clamp(stability + e.getValue());
                    case "romance"   -> romance   = clamp(romance + e.getValue());
                }
            }
            map.put(NpcCode.FATHER, new Relationship(NpcCode.FATHER, closeness, trust, stability, romance, old.lastInteractionDay(), old.broken()));
        }

        if ("alexander".equals(target) && !hasFatherChanges && map.containsKey(NpcCode.HUSBAND)) {
            Relationship old = map.get(NpcCode.HUSBAND);
            int closeness = old.closeness();
            int trust = old.trust();
            int stability = old.stability();
            int romance = old.romance();
            for (var e : changes.entrySet()) {
                switch (e.getKey()) {
                    case "closeness" -> closeness = clamp(closeness + e.getValue());
                    case "trust"     -> trust     = clamp(trust + e.getValue());
                    case "stability" -> stability = clamp(stability + e.getValue());
                    case "romance"   -> romance   = clamp(romance + e.getValue());
                }
            }
            map.put(NpcCode.HUSBAND, new Relationship(NpcCode.HUSBAND, closeness, trust, stability, romance, old.lastInteractionDay(), old.broken()));
        }

        relationships.set(new Relationships(map));
    }

    private void advanceTime(int hours) {
        GameTime gt = gameTime.get();
        if (gt.hasEnoughTime(hours)) {
            gameTime.set(gt.advanceHours(hours));
        } else {
            gameTime.set(gt.startNewDay());
        }
    }

    private void advanceQuestStep(String type, String target) {
        QuestLog log = questLog.get();
        for (Quest q : log.activeQuests()) {
            List<QuestStepState> steps = q.steps();
            for (int i = 0; i < steps.size(); i++) {
                QuestStepState step = steps.get(i);
                QuestObjective obj = step.objective();
                if (obj.type().equals(type) && obj.target().equals(target) && !step.isCompleted()) {
                    q.updateStep(i, step.increment());
                    q.checkCompletion();
                    return;
                }
            }
        }
    }

    private static int clamp(int v) {
        return Math.max(GameBalance.STAT_MIN, Math.min(GameBalance.STAT_MAX, v));
    }

    // =======================================================================
    // DTO builders
    // =======================================================================

    public GameStateDto buildGameStateDto() {
        return new GameStateDto(
                buildCharacterDto(),
                buildRelationshipsDto(),
                buildGameTimeDto(),
                buildActiveQuestDtos(),
                List.of()
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
        Map<String, RelationshipDto> byNpc = new LinkedHashMap<>();
        for (NpcCode code : NpcCode.values()) {
            Relationship r = rels.get(code);
            if (r != null) {
                byNpc.put(code.name(), new RelationshipDto(
                        code.name(), r.closeness(), r.trust(), r.stability(), r.romance(), r.broken()
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
                q.id(), q.type().name(), q.title(),
                q.isCompleted() ? "COMPLETED" : (q.isActive() ? "IN_PROGRESS" : "NOT_STARTED"),
                q.progressPercent(), steps
        );
    }

    // =======================================================================
    // Domain-object factories
    // =======================================================================

    private static PlayerCharacter buildTanya() {
        return new PlayerCharacter(
                new PlayerId("tanya-001"), "Tanya",
                Stats.initial(), JobInfo.initial(), Location.HOME,
                Map.of(), Skills.initial(), List.of()
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

        Quest selfCare = new Quest(
                "quest-self-care-01", QuestType.SELF_CARE_ARC,
                "Найди себя", "Позаботься о себе: отдохни и займись спортом",
                List.of(
                        new QuestStepState(new QuestObjective("rest", "sleep", 1, "Поспать не менее 7 часов"), 0),
                        new QuestStepState(new QuestObjective("rest", "gym", 1, "Сходить в спортзал"), 0)
                )
        );
        selfCare.start();
        log.addQuest(selfCare);

        Quest family = new Quest(
                "quest-family-01", QuestType.FAMILY_HARMONY,
                "Семейный вечер", "Укрепи отношения с мужем и папой",
                List.of(
                        new QuestStepState(new QuestObjective("social", "husband", 1, "Поговорить с мужем"), 0),
                        new QuestStepState(new QuestObjective("social", "father", 1, "Позвонить папе"), 0)
                )
        );
        family.start();
        log.addQuest(family);

        Quest career = new Quest(
                "quest-career-01", QuestType.CAREER_GROWTH,
                "Карьерный рост", "Выполни рабочий проект и получи признание",
                List.of(
                        new QuestStepState(new QuestObjective("work", "project", 3, "Завершить проект"), 0)
                )
        );
        log.addQuest(career);

        return log;
    }
}
