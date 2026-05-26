package roomescape.waiting.fake;

import java.util.HashMap;
import java.util.Map;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingThemeReference;

public class FakeWaitingThemeReference implements WaitingThemeReference {

    private final Map<Long, Theme> themes = new HashMap<>();

    @Override
    public Theme getTheme(Long themeId) {
        Theme theme = themes.get(themeId);
        if (theme == null) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_THEME_INVALID);
        }
        return theme;
    }

    public Theme save(Theme theme) {
        themes.put(theme.getId(), theme);
        return theme;
    }
}
