package roomescape.waiting.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;

@WebMvcTest(WaitingController.class)
class WaitingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WaitingService waitingService;

    @Test
    @DisplayName("POST /waitings - 정상 저장 시 201과 응답 본문을 반환한다")
    void createWaiting_success() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "테마A", "설명", "https://thumbnail.com");
        Waiting waiting = Waiting.createRow(1L, "브라운", date, time, theme, 2);
        WaitingCreateCommand command = new WaitingCreateCommand("브라운", date, 1L, 1L);
        given(waitingService.saveWaiting(command)).willReturn(waiting);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/waitings/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("브라운"))
                .andExpect(jsonPath("$.date").value("2026-05-05"))
                .andExpect(jsonPath("$.time.id").value(1))
                .andExpect(jsonPath("$.theme.id").value(1))
                .andExpect(jsonPath("$.sequence").value(2));

        then(waitingService).should().saveWaiting(command);
    }

    @Test
    @DisplayName("POST /waitings - 대기자 이름이 비어 있으면 에러 응답을 반환한다")
    void createWaiting_fail_with_empty_name() throws Exception {
        // given
        Map<String, Object> body = new HashMap<>();
        body.put("name", " ");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("대기자 이름을 입력해 주세요."));
    }

    @Test
    @DisplayName("POST /waitings - 예약되지 않은 슬롯이면 에러 응답을 반환한다")
    void createWaiting_fail_with_not_reserved_slot() throws Exception {
        // given
        LocalDate date = LocalDate.of(2026, 5, 5);
        WaitingCreateCommand command = new WaitingCreateCommand("브라운", date, 1L, 1L);
        willThrow(new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED))
                .given(waitingService)
                .saveWaiting(command);

        Map<String, Object> body = new HashMap<>();
        body.put("name", "브라운");
        body.put("date", "2026-05-05");
        body.put("timeId", 1);
        body.put("themeId", 1);

        // when & then
        mockMvc.perform(post("/waitings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약된 시간에만 대기를 신청할 수 있습니다."));
    }

    @Test
    @DisplayName("DELETE /waitings/me/{id} - 본인 대기 취소 요청을 서비스에 전달하고 204 응답을 반환한다")
    void deleteMyWaiting_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/waitings/me/1")
                        .param("name", "브라운"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        then(waitingService).should().cancelWaiting(1L, "브라운");
    }
}
