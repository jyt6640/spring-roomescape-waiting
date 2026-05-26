package roomescape.waiting.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingCreateRequest(
        @NotBlank(message = "대기자 이름을 입력해 주세요.")
        String name,

        @NotNull(message = "대기 날짜를 선택해 주세요.")
        LocalDate date,

        @NotNull(message = "대기 시간을 선택해 주세요.")
        Long timeId,

        @NotNull(message = "대기 테마를 선택해 주세요.")
        Long themeId
) {
}
