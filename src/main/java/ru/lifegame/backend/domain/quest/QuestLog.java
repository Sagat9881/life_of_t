package ru.lifegame.backend.domain.quest;

import java.util.*;

public class QuestLog {
    private final Map<String, Quest> quests;

    public QuestLog() {
        this.quests = new LinkedHashMap<>();
    }

    public void addQuest(Quest quest) {
        quests.put(quest.id(), quest);
    }

    public List<Quest> getActiveQuests() {
        return quests.values().stream().filter(Quest::isActive).toList();
    }

    public List<String> getCompletedQuestIds() {
        return quests.values().stream().filter(Quest::isCompleted).map(Quest::id).toList();
    }

    public Optional<Quest> findByType(QuestType type) {
        return quests.values().stream()
                .filter(q -> q.type() == type)
                .findFirst();
    }

    public boolean hasCompletedQuest(QuestType type) {
        return quests.values().stream()
                .anyMatch(q -> q.type() == type && q.isCompleted());
    }

    public Map<String, Quest> all() {
        return Collections.unmodifiableMap(quests);
    }
}
