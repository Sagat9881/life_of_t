package ru.lifegame.backend.domain.conflict.tactics;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.core.CspChanges;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public enum SkillBasedConflictTactics implements ConflictTactic {
    LISTEN_AND_UNDERSTAND("LISTEN_AND_UNDERSTAND", "Выслушать и понять",
            "Проявить эмпатию", "empathy", 30,
            "Спасибо, что выслушала...") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.LISTEN_PLAYER_CSP, GameBalance.LISTEN_OPPONENT_CSP);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, GameBalance.LISTEN_CLOSENESS, GameBalance.LISTEN_TRUST, 0, 0) : null;
            return new TacticEffects(csp, StatChanges.none(), rel, true, defaultReactionText());
        }
    },
    USE_HUMOR("USE_HUMOR", "Пошутить",
            "Разрядить обстановку юмором", "humor", 20,
            "Ха-ха, ладно, уговорила.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            boolean success = ThreadLocalRandom.current().nextInt(100) < GameBalance.HUMOR_SUCCESS_CHANCE;
            CspChanges csp = success
                    ? new CspChanges(GameBalance.HUMOR_PLAYER_CSP, GameBalance.HUMOR_OPPONENT_CSP)
                    : new CspChanges(5, 10);
            String reaction = success ? defaultReactionText() : "Это не смешно.";
            return new TacticEffects(csp, StatChanges.none(), null, success, reaction);
        }
    },
    LOGICAL_ARGUMENT("LOGICAL_ARGUMENT", "Логический аргумент",
            "Привести убедительные доводы", "rhetoric", 40,
            "Пожалуй, ты права.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.LOGICAL_PLAYER_CSP, GameBalance.LOGICAL_OPPONENT_CSP);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, 0, GameBalance.LOGICAL_TRUST, 0, 0) : null;
            return new TacticEffects(csp, StatChanges.none(), rel, true, defaultReactionText());
        }
    },
    EMOTIONAL_APPEAL("EMOTIONAL_APPEAL", "Эмоциональный призыв",
            "Апеллировать к чувствам", "charisma", 30,
            "Я люблю тебя, давай не будем ссориться.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.EMOTIONAL_PLAYER_CSP, GameBalance.EMOTIONAL_OPPONENT_CSP);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, GameBalance.EMOTIONAL_CLOSENESS, 0, 0, GameBalance.EMOTIONAL_ROMANCE) : null;
            return new TacticEffects(csp, StatChanges.none(), rel, true, defaultReactionText());
        }
    },
    SET_BOUNDARIES("SET_BOUNDARIES", "Установить границы",
            "Чётко обозначить свою позицию", "assertiveness", 50,
            "Я уважаю твоё мнение, но моё решение таково.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.BOUNDARIES_PLAYER_CSP, GameBalance.BOUNDARIES_OPPONENT_CSP);
            StatChanges stat = new StatChanges(0, 0, 0, 0, 0, GameBalance.BOUNDARIES_SELF_ESTEEM);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, 0, GameBalance.BOUNDARIES_TRUST, 0, 0) : null;
            return new TacticEffects(csp, stat, rel, true, defaultReactionText());
        }
    };

    private final String code;
    private final String label;
    private final String description;
    private final String skill;
    private final int skillLevel;
    private final String defaultReaction;

    SkillBasedConflictTactics(String code, String label, String description,
                               String skill, int skillLevel, String defaultReaction) {
        this.code = code; this.label = label; this.description = description;
        this.skill = skill; this.skillLevel = skillLevel; this.defaultReaction = defaultReaction;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public boolean isBaseAvailable() { return false; }
    @Override public Optional<String> requiredSkill() { return Optional.of(skill); }
    @Override public int requiredSkillLevel() { return skillLevel; }
    @Override public String defaultReactionText() { return defaultReaction; }
}
