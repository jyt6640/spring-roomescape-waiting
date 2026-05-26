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
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.fake.FakeWaitingReservationReference;
import roomescape.waiting.fake.FakeWaitingRepository;

class WaitingServiceTest {

    private WaitingRepository waitingRepository;
    private FakeWaitingReservationReference reservationReference;
    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationReference = new FakeWaitingReservationReference();
        waitingService = new WaitingService(
                waitingRepository,
                reservationReference,
                new WaitingValidator(waitingRepository)
        );
    }

    @Test
    @DisplayName("예약된 슬롯에 대기를 저장한다")
    void saveWaiting_success() {
        // given
        ReservationTime savedTime = ReservationTime.createRow(1L, LocalTime.now().plusHours(1));
        Theme savedTheme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now();
        reservationReference.setWaitingReservedSlot(new WaitingReservedSlot(1L, date, savedTime, savedTheme));
        WaitingCreateCommand command = new WaitingCreateCommand("리오", date, savedTime.getId(), savedTheme.getId());

        // when
        Waiting waiting = waitingService.saveWaiting(command);

        // then
        assertThat(waiting.getId()).isNotNull();
        assertThat(waiting.getReservationId()).isEqualTo(1L);
        assertThat(waiting.getName()).isEqualTo("리오");
        assertThat(waiting.getSequence()).isEqualTo(1);
        assertThat(waitingRepository.findById(waiting.getId())).contains(waiting);
    }

    @Test
    @DisplayName("같은 슬롯에 먼저 신청된 대기가 있으면 다음 순번으로 저장한다")
    void saveWaiting_success_with_next_sequence() {
        // given
        ReservationTime savedTime = ReservationTime.createRow(1L, LocalTime.now().plusHours(1));
        Theme savedTheme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        LocalDate date = LocalDate.now();
        reservationReference.setWaitingReservedSlot(new WaitingReservedSlot(1L, date, savedTime, savedTheme));
        waitingRepository.save(Waiting.create(1L, "리오", date, savedTime, savedTheme, 1));
        WaitingCreateCommand command = new WaitingCreateCommand("포비", date, savedTime.getId(), savedTheme.getId());

        // when
        Waiting waiting = waitingService.saveWaiting(command);

        // then
        assertThat(waiting.getSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("예약되지 않은 슬롯으로 대기하면 예외가 발생한다")
    void saveWaiting_fail_with_not_reserved_slot() {
        // given
        WaitingCreateCommand command = new WaitingCreateCommand(
                "리오",
                LocalDate.now().plusDays(1),
                1L,
                1L
        );
        reservationReference.setReservedSlot(false);

        // when & then
        assertThatThrownBy(() -> waitingService.saveWaiting(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약된 시간에만 대기를 신청할 수 있습니다.");
    }

    @Test
    @DisplayName("이름을 기반으로 자신의 대기 목록을 조회한다")
    void getWaitingsByName_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create(1L, "리오", LocalDate.now().plusDays(1), time, theme, 1)
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
                Waiting.create(1L, "리오", LocalDate.now().plusDays(1), time, theme, 1)
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
