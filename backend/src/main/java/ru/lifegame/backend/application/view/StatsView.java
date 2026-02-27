package ru.lifegame.backend.application.view;

public record StatsView(
        int energy,
        int health,
        int stress,
        int mood,
        int money,
        int selfEsteem
) {}
