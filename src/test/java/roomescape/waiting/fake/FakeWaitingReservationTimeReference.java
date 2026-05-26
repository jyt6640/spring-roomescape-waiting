package roomescape.waiting.fake;

import java.util.HashMap;
import java.util.Map;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.waiting.application.WaitingReservationTimeReference;

public class FakeWaitingReservationTimeReference implements WaitingReservationTimeReference {

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
