package roomescape.reservation.fake;

import java.util.HashMap;
import java.util.Map;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.application.ReservationTimeLookupReference;
import roomescape.reservationTime.domain.ReservationTime;

public class FakeReservationTimeLookupReference implements ReservationTimeLookupReference {

    private final Map<Long, ReservationTime> reservationTimes = new HashMap<>();

    @Override
    public ReservationTime getReservationTime(Long timeId) {
        ReservationTime reservationTime = reservationTimes.get(timeId);
        if (reservationTime == null) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_TIME_INVALID);
        }
        return reservationTime;
    }

    public ReservationTime save(ReservationTime reservationTime) {
        reservationTimes.put(reservationTime.getId(), reservationTime);
        return reservationTime;
    }
}
