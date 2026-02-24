package ru.lifegame.backend.application.view;

public record RelationshipView(String id, String npcCode, int closeness, int trust, int stability, int romance) {
}

record PetView(String id, String code, String name, int satiety, int attention, int health, int mood) {
}

record TimeView(int day, int hour) {
}
