package roomescape.theme.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.fake.FakeThemeRepository;

class WaitingThemeReferenceAdapterTest {

    private ThemeRepository themeRepository;
    private WaitingThemeReferenceAdapter referenceAdapter;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        referenceAdapter = new WaitingThemeReferenceAdapter(themeRepository);
    }

    @Test
    @DisplayName("테마를 조회한다")
    void getTheme_success() {
        // given
        Theme savedTheme = themeRepository.save(Theme.create("공포", "설명", "https://good.com"));

        // when
        Theme theme = referenceAdapter.getTheme(savedTheme.getId());

        // then
        assertThat(theme).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("존재하지 않는 테마이면 예외가 발생한다")
    void getTheme_fail_with_not_found_theme() {
        // when & then
        assertThatThrownBy(() -> referenceAdapter.getTheme(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("존재하지 않는 예약 테마입니다.");
    }
}
