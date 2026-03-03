package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.command.EndDayCommand;
import ru.lifegame.backend.application.view.GameStateView;

public interface EndDayUseCase {
    GameStateView execute(EndDayCommand command);
}
