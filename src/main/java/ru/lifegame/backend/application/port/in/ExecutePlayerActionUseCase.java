package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.view.GameStateView;

public interface ExecutePlayerActionUseCase {
    GameStateView execute(ExecuteActionCommand command);
}