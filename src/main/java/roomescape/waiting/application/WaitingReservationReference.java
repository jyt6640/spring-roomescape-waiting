package roomescape.waiting.application;

import roomescape.waiting.application.dto.WaitingCreateCommand;

public interface WaitingReservationReference {

    WaitingReservedSlot getReservedSlot(WaitingCreateCommand createCommand);
}
