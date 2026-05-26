package roomescape.theme.application;

import org.springframework.stereotype.Component;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.application.ReservationThemeReference;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Component
public class ReservationThemeReferenceAdapter implements ReservationThemeReference {

    private final ThemeRepository themeRepository;

    public ReservationThemeReferenceAdapter(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Override
    public Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_THEME_INVALID));
    }
}
