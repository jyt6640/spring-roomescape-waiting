package roomescape.waiting.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final WaitingReservationTimeReference reservationTimeReference;
    private final WaitingThemeReference themeReference;
    private final WaitingValidator waitingValidator;

    public WaitingService(
            WaitingRepository waitingRepository,
            WaitingReservationTimeReference reservationTimeReference,
            WaitingThemeReference themeReference,
            WaitingValidator waitingValidator
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeReference = reservationTimeReference;
        this.themeReference = themeReference;
        this.waitingValidator = waitingValidator;
    }

    @Transactional
    public Waiting saveWaiting(WaitingCreateCommand createCommand) {
        ReservationTime time = reservationTimeReference.getReservationTime(createCommand.timeId());
        Theme theme = themeReference.getTheme(createCommand.themeId());

        waitingValidator.validateWaitingAvailable(createCommand);
        int sequence = waitingRepository.countByDateAndTimeIdAndThemeId(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId()
        ) + 1;
        Waiting waiting = Waiting.create(
                null,
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
