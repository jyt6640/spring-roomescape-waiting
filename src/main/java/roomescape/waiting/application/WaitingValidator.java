package roomescape.waiting.application;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.waiting.domain.WaitingRepository;

@Component
public class WaitingValidator {

    private final WaitingRepository waitingRepository;

    public WaitingValidator(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public void validateDuplicateWaiting(LocalDate date, Long timeId, Long themeId, String name) {
        boolean exists = waitingRepository.findByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)
                .isPresent();
        if (exists) {
            throw new BusinessException(WaitingErrorCode.WAITING_ALREADY_EXISTS);
        }
    }
}
