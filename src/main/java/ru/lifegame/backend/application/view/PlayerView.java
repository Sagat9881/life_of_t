package ru.lifegame.backend.application.view;

import java.util.Map;

public record PlayerView(
        String id,
        String name,
        StatsView stats,
        JobView job,
        String location,
        Map<String, Boolean> tags,
        Map<String, Integer> skills,
        java.util.List<String> inventory
) {}

public record StatsView(int energy, int health, int stress, int mood, int money, int selfEsteem) {}

public record JobView(String title, int satisfaction, int burnoutRisk) {}
