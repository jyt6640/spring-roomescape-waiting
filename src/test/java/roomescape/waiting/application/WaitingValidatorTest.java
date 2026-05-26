package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingValidatorTest {

    private WaitingRepository waitingRepository;
    private WaitingValidator waitingValidator;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingValidator = new WaitingValidator(waitingRepository);
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다")
    void validateDuplicateWaiting_fail_with_duplicate_waiting() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create(1L, "리오", date, time, theme, 1));

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateDuplicateWaiting(1L, "리오"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 대기를 신청한 시간입니다.");
    }
}
