package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.command.ChooseConflictTacticCommand;
import ru.lifegame.backend.application.view.GameStateView;

public interface ChooseConflictTacticUseCase {
    GameStateView execute(ChooseConflictTacticCommand command);
}
