package roomescape.waiting.fake;

import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.application.WaitingReservationReference;
import roomescape.waiting.application.WaitingReservedSlot;
import roomescape.waiting.application.dto.WaitingCreateCommand;

public class FakeWaitingReservationReference implements WaitingReservationReference {

    private boolean reservedSlot = true;
    private WaitingReservedSlot waitingReservedSlot;

    @Override
    public WaitingReservedSlot getReservedSlot(WaitingCreateCommand createCommand) {
        if (!reservedSlot) {
            throw new BusinessException(WaitingErrorCode.WAITING_RESERVED_SLOT_REQUIRED);
        }
        if (waitingReservedSlot == null) {
            return new WaitingReservedSlot(
                    createCommand.date(),
                    ReservationTime.createRow(createCommand.timeId(), java.time.LocalTime.now().plusHours(1)),
                    Theme.createRow(createCommand.themeId(), "공포", "설명", "https://good.com")
            );
        }
        return waitingReservedSlot;
    }

    public void setReservedSlot(boolean reservedSlot) {
        this.reservedSlot = reservedSlot;
    }

    public void setWaitingReservedSlot(WaitingReservedSlot waitingReservedSlot) {
        this.waitingReservedSlot = waitingReservedSlot;
    }
}
