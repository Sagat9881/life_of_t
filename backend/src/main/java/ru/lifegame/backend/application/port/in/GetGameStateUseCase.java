package ru.lifegame.backend.application.port.in;

import ru.lifegame.backend.application.query.GetStateQuery;
import ru.lifegame.backend.application.view.GameStateView;

public interface GetGameStateUseCase {
    GameStateView execute(GetStateQuery query);
}
