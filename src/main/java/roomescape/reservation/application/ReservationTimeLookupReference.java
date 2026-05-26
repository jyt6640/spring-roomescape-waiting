package roomescape.reservation.application;

import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeLookupReference {

    ReservationTime getReservationTime(Long timeId);
}
