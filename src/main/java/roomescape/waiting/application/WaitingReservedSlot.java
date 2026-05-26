package roomescape.waiting.application;

import java.time.LocalDate;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record WaitingReservedSlot(
        Long reservationId,
        LocalDate date,
        ReservationTime time,
        Theme theme
) {
}
