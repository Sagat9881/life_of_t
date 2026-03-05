package backend.domain.scenario;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.action.impl.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.session.GameSession;

import static org.assertj.core.api.Assertions.assertThat;

class WorkaholicScenarioTest {


    @Test
    void workaholicPath_shouldLeadToRelationshipDeterioration() {
        GameSession session = GameSession.createNew("scenario_user");

        for (int day = 0; day < 14; day++) {
            session.executeAction(new GoToWorkAction());
            session.executeAction(new RestAtHomeAction());
            session.endDay();
        }

        assertThat(session.relationships().get(NpcCode.HUSBAND).closeness())
                .isLessThan(GameBalance.HUSBAND_CLOSENESS_ATTENTION);

        assertThat(session.player().consecutiveWorkDays()).isEqualTo(14);

        assertThat(session.player().stats().stress()).isLessThanOrEqualTo(20);

        assertThat(session.relationships().get(NpcCode.FATHER).closeness())
                .isLessThan(GameBalance.FATHER_INITIAL_CLOSENESS);
    }

    @Test
    void balancedPath_shouldMaintainRelationships() {
        GameSession session = GameSession.createNew("balanced_user");

        for (int day = 0; day < 14; day++) {
            session.executeAction(new GoToWorkAction());

            int cycle = day % 3;
            if (cycle == 0) {
                session.executeAction(new DateWithHusbandAction());
            } else if (cycle == 1) {
                session.executeAction(new VisitFatherAction());
            } else {
                session.executeAction(new WalkDogAction());
            }

            session.executeAction(new RestAtHomeAction());
            session.endDay();
        }

        assertThat(session.relationships().get(NpcCode.HUSBAND).closeness())
                .isGreaterThan(40);

        assertThat(session.relationships().get(NpcCode.FATHER).closeness())
                .isGreaterThan(30);

        assertThat(session.player().stats().stress()).isLessThan(70);
    }
}
