package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.*;

import java.util.Optional;

public enum BaseConflictTactics implements ConflictTactic {
    SURRENDER("SURRENDER", "Уступить", "Согласиться с оппонентом", "Ладно, ты прав(а)...") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.SURRENDER_PLAYER_CSP, GameBalance.SURRENDER_OPPONENT_CSP);
            StatChanges stat = new StatChanges(0, 0, 0, 0, 0, GameBalance.SURRENDER_SELF_ESTEEM);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, GameBalance.SURRENDER_CLOSENESS, GameBalance.SURRENDER_TRUST, 0, 0)
                    : null;
            return new TacticEffects(csp, stat, rel, true, defaultReactionText());
        }
    },
    ASSERT("ASSERT", "Настоять на своём", "Отстоять свою позицию", "Нет, я считаю иначе!") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.ASSERT_PLAYER_CSP, GameBalance.ASSERT_OPPONENT_CSP);
            StatChanges stat = new StatChanges(0, 0, 0, 0, 0, GameBalance.ASSERT_SELF_ESTEEM);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, GameBalance.ASSERT_CLOSENESS, GameBalance.ASSERT_TRUST, 0, 0)
                    : null;
            return new TacticEffects(csp, stat, rel, true, defaultReactionText());
        }
    },
    COMPROMISE("COMPROMISE", "Компромисс", "Найти компромисс", "Давай найдём решение вместе.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.COMPROMISE_PLAYER_CSP, GameBalance.COMPROMISE_OPPONENT_CSP);
            boolean success = player.skills().hasLevel("communication", 30)
                    || (conflict.type().opponent().isPresent()
                    && relationships.get(conflict.type().opponent().get()) != null
                    && relationships.get(conflict.type().opponent().get()).closeness() > 50);
            String reaction = success ? "Хорошо, давай попробуем." : "Это не компромисс, а полумера.";
            CspChanges finalCsp = success ? csp : new CspChanges(-5, 5);
            return new TacticEffects(finalCsp, StatChanges.none(), null, success, reaction);
        }
    },
    AVOID("AVOID", "Избежать разговора", "Уйти от конфликта", "Я не хочу об этом говорить.") {
        @Override
        public TacticEffects calculateEffects(PlayerCharacter player, Conflict conflict, Relationships relationships) {
            CspChanges csp = new CspChanges(GameBalance.AVOID_PLAYER_CSP, GameBalance.AVOID_OPPONENT_CSP);
            NpcCode npc = conflict.type().opponent().orElse(null);
            RelationshipChanges rel = npc != null
                    ? new RelationshipChanges(npc, GameBalance.AVOID_CLOSENESS, GameBalance.AVOID_TRUST, 0, 0)
                    : null;
            return new TacticEffects(csp, StatChanges.none(), rel, true, defaultReactionText());
        }
    };

    private final String code;
    private final String label;
    private final String description;
    private final String defaultReaction;

    BaseConflictTactics(String code, String label, String description, String defaultReaction) {
        this.code = code; this.label = label; this.description = description; this.defaultReaction = defaultReaction;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public boolean isBaseAvailable() { return true; }
    @Override public Optional<String> requiredSkill() { return Optional.empty(); }
    @Override public int requiredSkillLevel() { return 0; }
    @Override public String defaultReactionText() { return defaultReaction; }
}
