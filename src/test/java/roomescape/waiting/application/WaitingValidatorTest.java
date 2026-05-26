package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.fake.FakeWaitingReservationReference;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingValidatorTest {

    private WaitingRepository waitingRepository;
    private FakeWaitingReservationReference reservationReference;
    private WaitingValidator waitingValidator;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationReference = new FakeWaitingReservationReference();
        waitingValidator = new WaitingValidator(waitingRepository, reservationReference);
    }

    @Test
    @DisplayName("예약된 슬롯이면 대기를 신청할 수 있다")
    void validateWaitingAvailable_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingCreateCommand command = new WaitingCreateCommand("리오", date, time.getId(), theme.getId());

        // when & then
        assertThatCode(() -> waitingValidator.validateWaitingAvailable(command))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약되지 않은 슬롯에는 대기를 신청할 수 없다")
    void validateWaitingAvailable_fail_with_not_reserved_slot() {
        // given
        WaitingCreateCommand command = new WaitingCreateCommand(
                "리오",
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        reservationReference.setReservedSlot(false);

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateWaitingAvailable(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약된 시간에만 대기를 신청할 수 있습니다.");
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다")
    void validateWaitingAvailable_fail_with_duplicate_waiting() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        waitingRepository.save(Waiting.create("리오", date, time, theme, 1));
        WaitingCreateCommand command = new WaitingCreateCommand("리오", date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> waitingValidator.validateWaitingAvailable(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 대기를 신청한 시간입니다.");
    }
}
