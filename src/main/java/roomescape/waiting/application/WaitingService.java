package roomescape.waiting.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingValidator waitingValidator;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingValidator waitingValidator
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingValidator = waitingValidator;
    }

    @Transactional
    public Waiting saveWaiting(WaitingCreateCommand createCommand) {
        ReservationTime time = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_TIME_INVALID));
        Theme theme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_THEME_INVALID));

        waitingValidator.validateWaitingAvailable(createCommand);
        int sequence = waitingRepository.countByDateAndTimeIdAndThemeId(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId()
        ) + 1;
        Waiting waiting = Waiting.create(
                createCommand.name(),
                createCommand.date(),
                time,
                theme,
                sequence
        );
        return waitingRepository.save(waiting);
    }

    public List<Waiting> getWaitingsByName(String name) {
        return waitingRepository.findByName(name);
    }

    @Transactional
    public void cancelWaiting(Long id, String name) {
        Waiting targetWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, id));
        targetWaiting.cancel(name);
        waitingRepository.deleteByIdAndName(id, name);
    }
}
