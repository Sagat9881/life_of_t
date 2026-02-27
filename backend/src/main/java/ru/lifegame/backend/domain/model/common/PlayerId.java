package ru.lifegame.backend.domain.model.common;

import java.util.UUID;

public record PlayerId(String value) {
    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID().toString());
    }
}
