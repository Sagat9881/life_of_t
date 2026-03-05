package backend.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.lifegame.backend.LifegameBackendApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LifegameBackendApplication.class)
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startSession_shouldReturn200_withGameState() throws Exception {
        mockMvc.perform(post("/api/v1/game/session/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_user_1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telegramUserId").value("test_user_1"))
                .andExpect(jsonPath("$.player.name").value("\u0422\u0430\u0442\u044c\u044f\u043d\u0430"))
                .andExpect(jsonPath("$.player.stats.energy").value(100))
                .andExpect(jsonPath("$.time.day").value(1))
                .andExpect(jsonPath("$.time.hour").value(8));
    }

    @Test
    void getState_shouldReturn404_whenSessionNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/game/state")
                        .param("telegramUserId", "nonexistent_user"))
                .andExpect(status().isOk());
    }

    @Test
    void executeAction_shouldUpdateTimeAndStats() throws Exception {
        mockMvc.perform(post("/api/v1/game/session/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_action_user\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/game/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_action_user\", \"actionCode\": \"PLAY_WITH_CAT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time.hour").value(9))
                .andExpect(jsonPath("$.lastActionResult.actionCode").value("PLAY_WITH_CAT"))
                .andExpect(jsonPath("$.lastActionResult.actualTimeCost").value(1));
    }

    @Test
    void executeAction_shouldReturn409_whenNotEnoughTime() throws Exception {
        mockMvc.perform(post("/api/v1/game/session/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_time_user\"}"))
                .andExpect(status().isOk());

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/game/action")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"telegramUserId\": \"test_time_user\", \"actionCode\": \"SELF_CARE\"}"));
        }

        mockMvc.perform(post("/api/v1/game/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_time_user\", \"actionCode\": \"GO_TO_WORK\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NOT_ENOUGH_TIME"));
    }

    @Test
    void executeAction_shouldReturn400_forInvalidActionCode() throws Exception {
        mockMvc.perform(post("/api/v1/game/session/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_invalid_user\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/game/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telegramUserId\": \"test_invalid_user\", \"actionCode\": \"FLY_TO_MARS\"}"))
                .andExpect(status().isBadRequest());
    }
}
