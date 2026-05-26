package roomescape.waiting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.ReservationTimeJdbcTemplateRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeJdbcTemplateRepository;
import roomescape.waiting.domain.Waiting;

@JdbcTest
@Import({
        WaitingJdbcTemplateRepository.class,
        ReservationTimeJdbcTemplateRepository.class,
        ThemeJdbcTemplateRepository.class
})
class WaitingJdbcTemplateRepositoryTest {

    private static final LocalDate DATE = LocalDate.now().plusDays(1);

    private final WaitingJdbcTemplateRepository waitingRepository;
    private final ReservationTimeJdbcTemplateRepository timeRepository;
    private final ThemeJdbcTemplateRepository themeRepository;

    private ReservationTime savedTime;
    private Theme savedTheme;

    @Autowired
    WaitingJdbcTemplateRepositoryTest(
            WaitingJdbcTemplateRepository waitingRepository,
            ReservationTimeJdbcTemplateRepository timeRepository,
            ThemeJdbcTemplateRepository themeRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @BeforeEach
    void setUp() {
        savedTime = timeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(Theme.create("공포", "설명", "https://good.com"));
    }

    @Test
    @DisplayName("대기를 저장하면 생성된 ID를 포함한 대기 객체를 반환한다")
    void save_success() {
        // given
        Waiting waiting = Waiting.create("브라운", DATE, savedTime, savedTheme, 1);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getName()).isEqualTo("브라운");
        assertThat(savedWaiting.getSequence()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 슬롯에 등록된 대기 수를 조회한다")
    void countByDateAndTimeIdAndThemeId_success() {
        // given
        waitingRepository.save(Waiting.create("브라운", DATE, savedTime, savedTheme, 1));
        waitingRepository.save(Waiting.create("리오", DATE, savedTime, savedTheme, 2));

        // when
        int count = waitingRepository.countByDateAndTimeIdAndThemeId(
                DATE,
                savedTime.getId(),
                savedTheme.getId()
        );

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("이름을 기반으로 대기 목록과 현재 순번을 조회한다")
    void findByName_success_with_current_sequence() {
        // given
        Waiting first = waitingRepository.save(Waiting.create("브라운", DATE, savedTime, savedTheme, 1));
        Waiting second = waitingRepository.save(Waiting.create("리오", DATE, savedTime, savedTheme, 2));
        Waiting third = waitingRepository.save(Waiting.create("포비", DATE, savedTime, savedTheme, 3));
        waitingRepository.deleteByIdAndName(first.getId(), "브라운");

        // when
        List<Waiting> waitings = waitingRepository.findByName("리오");

        // then
        assertThat(waitings).hasSize(1);
        assertThat(waitings.get(0).getId()).isEqualTo(second.getId());
        assertThat(waitings.get(0).getSequence()).isEqualTo(1);
        assertThat(waitingRepository.findById(third.getId()).orElseThrow().getSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("date, timeId, themeId, name이 모두 일치하는 대기를 조회한다")
    void findByDateAndTimeIdAndThemeIdAndName_success() {
        // given
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", DATE, savedTime, savedTheme, 1)
        );

        // when
        Optional<Waiting> result = waitingRepository.findByDateAndTimeIdAndThemeIdAndName(
                DATE,
                savedTime.getId(),
                savedTheme.getId(),
                "브라운"
        );

        // then
        assertThat(result).contains(savedWaiting);
    }

    @Test
    @DisplayName("이름과 대기 ID를 기반으로 자신의 대기를 삭제한다")
    void deleteByIdAndName_success() {
        // given
        Waiting savedWaiting = waitingRepository.save(
                Waiting.create("브라운", DATE, savedTime, savedTheme, 1)
        );

        // when
        waitingRepository.deleteByIdAndName(savedWaiting.getId(), "브라운");

        // then
        assertThat(waitingRepository.findById(savedWaiting.getId())).isEmpty();
    }
}
