package roomescape.waiting.application;

import roomescape.reservationTime.domain.ReservationTime;

public interface WaitingReservationTimeReference {

    ReservationTime getReservationTime(Long timeId);
}
