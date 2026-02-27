package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.command.ChooseEventOptionCommand;
import ru.lifegame.backend.application.view.GameStateView;

public interface ChooseEventOptionUseCase {
    GameStateView execute(ChooseEventOptionCommand command);
}