package ru.lifegame.backend.application.view;

public record QuestView(String id, String title, String description, int progress, boolean isCompleted) {}