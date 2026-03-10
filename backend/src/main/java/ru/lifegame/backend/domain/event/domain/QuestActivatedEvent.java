package ru.lifegame.backend.domain.event.domain;

/**
 * Published when a quest is auto-activated by EndDayService because its
 * triggerDay has arrived. Frontend uses this to show a quest-start
 * notification or cutscene.
 *
 * @param sessionId  the game session that activated the quest
 * @param questId    machine-readable quest id (matches QuestSpec.id())
 * @param questTitle localised quest title shown to the player
 */
public record QuestActivatedEvent(
        String sessionId,
        String questId,
        String questTitle
) implements DomainEvent {

    @Override
    public String type() {
        return "QUEST_ACTIVATED";
    }
}
