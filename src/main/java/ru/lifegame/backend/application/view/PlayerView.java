package ru.lifegame.backend.application.view;

import java.util.List;
import java.util.Map;

public record PlayerView(
        String id, String name, StatsView stats, JobView job,
        String location, Map<String, Boolean> tags,
        Map<String, Integer> skills, List<String> inventory
) {}