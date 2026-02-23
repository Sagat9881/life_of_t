package ru.lifegame.backend.application.view;

public record RelationshipView(String id, String npcCode, int closeness, int trust, int stability, int romance) {}