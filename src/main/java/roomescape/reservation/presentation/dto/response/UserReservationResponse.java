package roomescape.reservation.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.presentation.dto.response.ReservationTimeResponse;
import roomescape.theme.presentation.dto.response.ThemeResponse;
import roomescape.waiting.domain.Waiting;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserReservationResponse(
        Long id,
        String name,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Integer sequence
) {
    private static final String RESERVED_STATUS = "RESERVED";
    private static final String WAITING_STATUS = "WAITING";

    public static UserReservationResponse fromReservation(Reservation reservation) {
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                RESERVED_STATUS,
                null
        );
    }

    public static UserReservationResponse fromWaiting(Waiting waiting) {
        return new UserReservationResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate().toString(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                WAITING_STATUS,
                waiting.getSequence()
        );
    }
}
