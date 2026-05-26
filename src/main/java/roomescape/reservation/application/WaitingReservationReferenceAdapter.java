package roomescape.reservation.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.WaitingReservationReference;
import roomescape.waiting.application.WaitingReservedSlot;
import roomescape.waiting.application.dto.WaitingCreateCommand;

@Component
public class WaitingReservationReferenceAdapter implements WaitingReservationReference {

    private final ReservationRepository reservationRepository;

    public WaitingReservationReferenceAdapter(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public WaitingReservedSlot getReservedSlot(WaitingCreateCommand createCommand) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                createCommand.date(),
                createCommand.timeId(),
                createCommand.themeId()
        ).orElseThrow(() -> new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED));

        return new WaitingReservedSlot(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }
}
