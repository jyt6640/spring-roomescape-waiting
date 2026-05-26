package roomescape.reservation.application;

import roomescape.theme.domain.Theme;

public interface ReservationThemeReference {

    Theme getTheme(Long themeId);
}
