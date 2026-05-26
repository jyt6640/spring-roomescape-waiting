package roomescape.reservationTime.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.waiting.application.WaitingReservationTimeReference;

@Component
public class WaitingReservationTimeReferenceAdapter implements WaitingReservationTimeReference {

    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingReservationTimeReferenceAdapter(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Override
    public ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_TIME_INVALID));
    }
}
