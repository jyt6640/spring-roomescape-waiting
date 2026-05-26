package roomescape.reservation.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.WaitingReservationReference;
import roomescape.waiting.application.dto.WaitingCreateCommand;

@Component
public class WaitingReservationReferenceAdapter implements WaitingReservationReference {

    private final ReservationRepository reservationRepository;

    public WaitingReservationReferenceAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void validateReservedSlot(WaitingCreateCommand createCommand) {
        boolean reserved = reservationRepository.findByDateAndTimeIdAndThemeId(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId()
        ).isPresent();
        if (!reserved) {
            throw new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED);
        }
    }
}
