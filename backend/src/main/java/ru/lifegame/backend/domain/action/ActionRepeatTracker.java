package ru.lifegame.backend.domain.action;

import java.util.LinkedList;

public class ActionRepeatTracker {
    private final LinkedList<String> recentActions = new LinkedList<>();
    private static final int HISTORY_SIZE = 5;
    private static final int REPEAT_THRESHOLD = 3;
    private static final double DIMINISHING_FACTOR = 0.7;

    public void record(String actionCode) {
        recentActions.addLast(actionCode);
        if (recentActions.size() > HISTORY_SIZE) {
            recentActions.removeFirst();
        }
    }

    public double getEfficiencyMultiplier(String actionCode) {
        long consecutiveCount = 0;
        var iter = recentActions.descendingIterator();
        while (iter.hasNext()) {
            if (iter.next().equals(actionCode)) consecutiveCount++;
            else break;
        }
        if (consecutiveCount < REPEAT_THRESHOLD) return 1.0;
        int overRepeat = (int)(consecutiveCount - REPEAT_THRESHOLD + 1);
        return Math.pow(DIMINISHING_FACTOR, overRepeat);
    }
}
