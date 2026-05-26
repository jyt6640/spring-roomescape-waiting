package roomescape.waiting.fake;

import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.waiting.application.WaitingReservationReference;
import roomescape.waiting.application.dto.WaitingCreateCommand;

public class FakeWaitingReservationReference implements WaitingReservationReference {

    private boolean reservedSlot = true;

    @Override
    public void validateReservedSlot(WaitingCreateCommand createCommand) {
        if (!reservedSlot) {
            throw new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED);
        }
    }

    public void setReservedSlot(boolean reservedSlot) {
        this.reservedSlot = reservedSlot;
    }
}
