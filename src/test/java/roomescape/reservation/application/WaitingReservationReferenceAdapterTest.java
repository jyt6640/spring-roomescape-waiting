package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingReservedSlot;
import roomescape.waiting.application.dto.WaitingCreateCommand;

class WaitingReservationReferenceAdapterTest {

    private ReservationRepository reservationRepository;
    private WaitingReservationReferenceAdapter referenceAdapter;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        referenceAdapter = new WaitingReservationReferenceAdapter(reservationRepository);
    }

    @Test
    @DisplayName("예약된 슬롯을 조회한다")
    void getReservedSlot_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation reservation = reservationRepository.save(Reservation.create("브라운", date, time, theme));
        WaitingCreateCommand command = new WaitingCreateCommand("리오", date, time.getId(), theme.getId());

        // when
        WaitingReservedSlot reservedSlot = referenceAdapter.getReservedSlot(command);

        // then
        assertThat(reservedSlot.date()).isEqualTo(date);
        assertThat(reservedSlot.time()).isEqualTo(time);
        assertThat(reservedSlot.theme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("예약되지 않은 슬롯이면 예외가 발생한다")
    void getReservedSlot_fail_with_not_reserved_slot() {
        // given
        WaitingCreateCommand command = new WaitingCreateCommand(
                "리오",
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        // when & then
        assertThatThrownBy(() -> referenceAdapter.getReservedSlot(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약된 시간에만 대기를 신청할 수 있습니다.");
    }
}
