package ru.lifegame.backend.domain.scenario;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.action.impl.*;
import ru.lifegame.backend.domain.model.GameSession;
import ru.lifegame.backend.domain.model.NpcCode;

import static org.assertj.core.api.Assertions.assertThat;

class WorkaholicScenarioTest {

    @Test
    void workaholicPath_shouldLeadToBurnoutOrDivorce() {
        GameSession session = GameSession.createNew("scenario_user");

        for (int day = 0; day < 14; day++) {
            session.executeAction(new GoToWorkAction());
            session.executeAction(new RestAtHomeAction());
            session.endDay();
        }

        assertThat(session.player().stats().stress()).isGreaterThan(50);
        assertThat(session.relationships().get(NpcCode.HUSBAND).closeness()).isLessThan(50);
        assertThat(session.player().job().burnoutRisk()).isGreaterThan(40);
    }

    @Test
    void balancedPath_shouldMaintainRelationships() {
        GameSession session = GameSession.createNew("balanced_user");

        for (int day = 0; day < 14; day++) {
            session.executeAction(new GoToWorkAction());
            if (day % 2 == 0) session.executeAction(new DateWithHusbandAction());
            if (day % 3 == 0) session.executeAction(new VisitFatherAction());
            session.executeAction(new WalkDogAction());
            session.endDay();
        }

        assertThat(session.relationships().get(NpcCode.HUSBAND).closeness()).isGreaterThan(40);
        assertThat(session.relationships().get(NpcCode.FATHER).closeness()).isGreaterThan(30);
        assertThat(session.player().stats().stress()).isLessThan(70);
    }
}
