package ru.lifegame.backend.infrastructure.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class SessionLockManager {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithLock(String userId, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(userId, k -> new ReentrantLock(true));
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void executeWithLock(String userId, Runnable action) {
        executeWithLock(userId, () -> { action.run(); return null; });
    }

    public void cleanup(String userId) {
        ReentrantLock lock = locks.get(userId);
        if (lock != null && !lock.isLocked()) {
            locks.remove(userId);
        }
    }
}
