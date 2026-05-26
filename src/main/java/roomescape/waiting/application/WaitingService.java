package roomescape.waiting.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final WaitingReservationReference reservationReference;
    private final WaitingValidator waitingValidator;

    public WaitingService(
            WaitingRepository waitingRepository,
            WaitingReservationReference reservationReference,
            WaitingValidator waitingValidator
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationReference = reservationReference;
        this.waitingValidator = waitingValidator;
    }

    @Transactional
    public Waiting saveWaiting(WaitingCreateCommand createCommand) {
        WaitingReservedSlot reservedSlot = reservationReference.getReservedSlot(createCommand);

        waitingValidator.validateDuplicateWaiting(
                reservedSlot.date(),
                reservedSlot.time().getId(),
                reservedSlot.theme().getId(),
                createCommand.name()
        );
        int sequence = waitingRepository.countByDateAndTimeIdAndThemeId(
                reservedSlot.date(),
                reservedSlot.time().getId(),
                reservedSlot.theme().getId()
        ) + 1;
        Waiting waiting = Waiting.create(
                createCommand.name(),
                reservedSlot.date(),
                reservedSlot.time(),
                reservedSlot.theme(),
                sequence
        );
        return waitingRepository.save(waiting);
    }

    public List<Waiting> getWaitingsByName(String name) {
        return waitingRepository.findByName(name);
    }

    @Transactional
    public void cancelWaiting(Long id, String name) {
        Waiting targetWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(WaitingErrorCode.WAITING_NOT_FOUND, id));
        targetWaiting.cancel(name);
        waitingRepository.deleteByIdAndName(id, name);
    }
}
