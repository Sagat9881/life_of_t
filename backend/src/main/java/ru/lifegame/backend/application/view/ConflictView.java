package ru.lifegame.backend.application.view;

import java.util.List;

public record ConflictView(String id, String typeCode, String label, String stage,
                            int playerCsp, int opponentCsp, List<TacticOptionView> availableTactics) {}