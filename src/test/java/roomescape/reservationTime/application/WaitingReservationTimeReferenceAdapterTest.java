package roomescape.reservationTime.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.fake.FakeReservationTimeRepository;

class WaitingReservationTimeReferenceAdapterTest {

    private ReservationTimeRepository reservationTimeRepository;
    private WaitingReservationTimeReferenceAdapter referenceAdapter;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        referenceAdapter = new WaitingReservationTimeReferenceAdapter(reservationTimeRepository);
    }

    @Test
    @DisplayName("예약 시간을 조회한다")
    void getReservationTime_success() {
        // given
        ReservationTime savedTime = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        // when
        ReservationTime reservationTime = referenceAdapter.getReservationTime(savedTime.getId());

        // then
        assertThat(reservationTime).isEqualTo(savedTime);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간이면 예외가 발생한다")
    void getReservationTime_fail_with_not_found_time() {
        // when & then
        assertThatThrownBy(() -> referenceAdapter.getReservationTime(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 시간입니다.");
    }
}
