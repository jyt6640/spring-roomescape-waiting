package roomescape.waiting.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.WaitingRepository;

@Component
public class WaitingValidator {

    private final WaitingRepository waitingRepository;
    private final WaitingReservationReference reservationReference;

    public WaitingValidator(WaitingRepository waitingRepository, WaitingReservationReference reservationReference) {
        this.waitingRepository = waitingRepository;
        this.reservationReference = reservationReference;
    }

    public void validateWaitingAvailable(WaitingCreateCommand createCommand) {
        validateReservedSlot(createCommand);
        validateDuplicateWaiting(createCommand);
    }

    private void validateReservedSlot(WaitingCreateCommand createCommand) {
        reservationReference.getReservedSlot(createCommand);
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
