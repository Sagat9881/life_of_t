package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.command.StartSessionCommand;
import ru.lifegame.backend.application.view.GameStateView;

public interface StartOrLoadSessionUseCase {
    GameStateView execute(StartSessionCommand command);
}
