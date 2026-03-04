package backend.domain.scenario;

import org.junit.jupiter.api.Test;
import ru.lifegame.backend.domain.action.impl.*;
import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.session.GameSession;

import static org.assertj.core.api.Assertions.assertThat;

class WorkaholicScenarioTest {

    /**
     * \u0421\u0446\u0435\u043d\u0430\u0440\u0438\u0439 \u00ab\u0422\u0440\u0443\u0434\u043e\u0433\u043e\u043b\u0438\u043a\u00bb: 14 \u0434\u043d\u0435\u0439 \u0442\u043e\u043b\u044c\u043a\u043e \u0440\u0430\u0431\u043e\u0442\u0430 + \u043e\u0442\u0434\u044b\u0445.
     * \u0420\u0430\u0431\u043e\u0442\u0430 \u043a\u0430\u0436\u0434\u044b\u0439 \u0434\u0435\u043d\u044c \u043f\u043e\u043b\u043d\u043e\u0441\u0442\u044c\u044e \u043a\u043e\u043c\u043f\u0435\u043d\u0441\u0438\u0440\u0443\u0435\u0442 \u0441\u0442\u0440\u0435\u0441\u0441 \u043e\u0442\u0434\u044b\u0445\u043e\u043c,
     * \u043d\u043e \u043e\u0442\u043d\u043e\u0448\u0435\u043d\u0438\u044f \u0441 \u043c\u0443\u0436\u0435\u043c \u0434\u0435\u0433\u0440\u0430\u0434\u0438\u0440\u0443\u044e\u0442 \u0438\u0437-\u0437\u0430 \u043e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0438\u044f \u0432\u043d\u0438\u043c\u0430\u043d\u0438\u044f.
     */
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

    /**
     * \u0421\u0446\u0435\u043d\u0430\u0440\u0438\u0439 \u00ab\u0411\u0430\u043b\u0430\u043d\u0441\u00bb: 14 \u0434\u043d\u0435\u0439 \u0440\u0430\u0431\u043e\u0442\u044b \u0441 3-\u0434\u043d\u0435\u0432\u043d\u044b\u043c \u0446\u0438\u043a\u043b\u043e\u043c:
     * \u0434\u0435\u043d\u044c 0: \u0440\u0430\u0431\u043e\u0442\u0430 + \u0441\u0432\u0438\u0434\u0430\u043d\u0438\u0435 \u0441 \u043c\u0443\u0436\u0435\u043c + \u043e\u0442\u0434\u044b\u0445
     * \u0434\u0435\u043d\u044c 1: \u0440\u0430\u0431\u043e\u0442\u0430 + \u0432\u0438\u0437\u0438\u0442 \u043a \u043e\u0442\u0446\u0443 + \u043e\u0442\u0434\u044b\u0445
     * \u0434\u0435\u043d\u044c 2: \u0440\u0430\u0431\u043e\u0442\u0430 + \u043f\u0440\u043e\u0433\u0443\u043b\u043a\u0430 \u0441 \u0441\u043e\u0431\u0430\u043a\u043e\u0439 + \u043e\u0442\u0434\u044b\u0445
     */
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
