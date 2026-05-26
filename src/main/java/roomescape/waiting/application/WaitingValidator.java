package roomescape.waiting.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.WaitingRepository;

@Component
public class WaitingValidator {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingValidator(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public void validateWaitingAvailable(WaitingCreateCommand createCommand) {
        validateReservedSlot(createCommand);
        validateDuplicateWaiting(createCommand);
    }

    private void validateReservedSlot(WaitingCreateCommand createCommand) {
        boolean reserved = reservationRepository.findByDateAndTimeIdAndThemeId(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId()
        ).isPresent();
        if (!reserved) {
            throw new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED);
        }
    }

    private void validateDuplicateWaiting(WaitingCreateCommand createCommand) {
        boolean exists = waitingRepository.findByDateAndTimeIdAndThemeIdAndName(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId(),
                createCommand.name()
        ).isPresent();
        if (exists) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }
}
