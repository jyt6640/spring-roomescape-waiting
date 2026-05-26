package roomescape.waiting.application;

import roomescape.waiting.application.dto.WaitingCreateCommand;

public interface WaitingReservationReference {

    void validateReservedSlot(WaitingCreateCommand createCommand);
}
