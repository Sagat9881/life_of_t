package ru.lifegame.backend.application.view;

public record PetView(String id, String code, String name, int satiety, int attention, int health, int mood) {}