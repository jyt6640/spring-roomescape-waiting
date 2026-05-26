package roomescape.waiting.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.fake.FakeWaitingReservationReference;
import roomescape.waiting.fake.FakeWaitingReservationTimeReference;
import roomescape.waiting.fake.FakeWaitingRepository;
import roomescape.waiting.fake.FakeWaitingThemeReference;

class WaitingServiceTest {

    private WaitingRepository waitingRepository;
    private ReservationRepository reservationRepository;
    private FakeWaitingReservationTimeReference reservationTimeReference;
    private FakeWaitingThemeReference themeReference;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationRepository = new FakeReservationRepository();
        reservationTimeReference = new FakeWaitingReservationTimeReference();
        themeReference = new FakeWaitingThemeReference();
        waitingService = new WaitingService(
                waitingRepository,
                reservationTimeReference,
                themeReference,
                new WaitingValidator(waitingRepository, new FakeWaitingReservationReference())
        );
    }

    @Test
    @DisplayName("예약된 슬롯에 대기를 저장한다")
    void saveWaiting_success() {
        // given
        ReservationTime savedTime = reservationTimeReference.save(
                ReservationTime.createRow(1L, LocalTime.now().plusHours(1))
        );
        Theme savedTheme = themeReference.save(Theme.createRow(1L, "공포", "설명", "https://good.com"));
        LocalDate date = LocalDate.now();
        reservationRepository.save(Reservation.create("브라운", date, savedTime, savedTheme));
        WaitingCreateCommand command = new WaitingCreateCommand("리오", date, savedTime.getId(), savedTheme.getId());

        // when
        Waiting waiting = waitingService.saveWaiting(command);

        // then
        assertThat(waiting.getId()).isNotNull();
        assertThat(waiting.getName()).isEqualTo("리오");
        assertThat(waiting.getSequence()).isEqualTo(1);
        assertThat(waitingRepository.findById(waiting.getId())).contains(waiting);
    }

    @Test
    @DisplayName("같은 슬롯에 먼저 신청된 대기가 있으면 다음 순번으로 저장한다")
    void saveWaiting_success_with_next_sequence() {
        // given
        ReservationTime savedTime = reservationTimeReference.save(
                ReservationTime.createRow(1L, LocalTime.now().plusHours(1))
        );
        Theme savedTheme = themeReference.save(Theme.createRow(1L, "공포", "설명", "https://good.com"));
        LocalDate date = LocalDate.now();
        reservationRepository.save(Reservation.create("브라운", date, savedTime, savedTheme));
        waitingRepository.save(Waiting.create("리오", date, savedTime, savedTheme, 1));
        WaitingCreateCommand command = new WaitingCreateCommand("포비", date, savedTime.getId(), savedTheme.getId());

        // when
        Waiting waiting = waitingService.saveWaiting(command);

        // then
        assertThat(waiting.getSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 대기하면 예외가 발생한다")
    void saveWaiting_fail_with_not_found_time() {
        // given
        Theme savedTheme = themeReference.save(Theme.createRow(1L, "공포", "설명", "https://good.com"));
        WaitingCreateCommand command = new WaitingCreateCommand(
                "리오",
                LocalDate.now().plusDays(1),
                999L,
                savedTheme.getId()
        );

        // when & then
        assertThatThrownBy(() -> waitingService.saveWaiting(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 시간입니다.");
    }

    @Test
    @DisplayName("이름을 기반으로 자신의 대기 목록을 조회한다")
    void getWaitingsByName_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("리오", LocalDate.now().plusDays(1), time, theme, 1)
        );

        // when
        List<Waiting> waitings = waitingService.getWaitingsByName("리오");

        // then
        assertThat(waitings).containsExactly(savedWaiting);
    }

    @Test
    @DisplayName("이름을 기반으로 자신의 대기를 취소한다")
    void cancelWaiting_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("리오", LocalDate.now().plusDays(1), time, theme, 1)
        );

        // when
        waitingService.cancelWaiting(savedWaiting.getId(), "리오");

        // then
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 대기 ID로 취소하면 예외가 발생한다")
    void cancelWaiting_fail_with_not_found_waiting() {
        // when & then
        assertThatThrownBy(() -> waitingService.cancelWaiting(999L, "리오"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("대기를 찾을 수 없습니다.");
    }
}
