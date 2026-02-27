package ru.lifegame.backend.application.view;

import java.util.List;

public record EventView(String id, String title, String description, List<EventOptionView> options) {}