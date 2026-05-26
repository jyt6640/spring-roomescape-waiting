package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class WaitingTest {

    @Test
    @DisplayName("대기를 생성한다")
    void create_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");

        // when
        Waiting waiting = Waiting.create(1L, "브라운", LocalDate.now().plusDays(1), time, theme, 1);

        // then
        assertThat(waiting.getReservationId()).isEqualTo(1L);
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getSequence()).isEqualTo(1);
    }

    @Test
    @DisplayName("지난 일정으로 대기를 생성할 수 없다")
    void create_fail_with_past_date_time() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");

        // when & then
        assertThatThrownBy(() -> Waiting.create(1L, "브라운", LocalDate.now().minusDays(1), time, theme, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지난 일정으로 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("본인의 대기이면 취소할 수 있다")
    void cancel_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Waiting waiting = Waiting.createRow(1L, 1L, "브라운", LocalDate.now().plusDays(1), time, theme, 1);

        // when & then
        assertThatCode(() -> waiting.cancel("브라운"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("본인의 대기가 아니면 취소할 수 없다")
    void cancel_fail_with_invalid_owner() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Waiting waiting = Waiting.createRow(1L, 1L, "브라운", LocalDate.now().plusDays(1), time, theme, 1);

        // when & then
        assertThatThrownBy(() -> waiting.cancel("리오"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("대기를 취소할 수 있는 권한이 없습니다.");
    }
}
