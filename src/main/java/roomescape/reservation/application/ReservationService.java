package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeLookupReference reservationTimeReference;
    private final ReservationThemeReference themeReference;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeLookupReference reservationTimeReference,
            ReservationThemeReference themeReference,
            ReservationValidator reservationValidator
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeReference = reservationTimeReference;
        this.themeReference = themeReference;
        this.reservationValidator = reservationValidator;
    }

    @Transactional
    public Reservation saveReservation(ReservationCreateCommand createCommand) {
        ReservationTime time = reservationTimeReference.getReservationTime(createCommand.timeId());
        Theme theme = themeReference.getTheme(createCommand.themeId());

        reservationValidator.validateAlreadyReservation(createCommand);
        Reservation reservation = Reservation.create(
                createCommand.name(),
                createCommand.date(),
                time,
                theme
        );
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByDateAndTheme(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId);
    }

    public List<Reservation> getReservationsByName(String name) {
        return reservationRepository.findByName(name);
    }

    @Transactional
    public void updateReservationSchedule(ReservationUpdateCommand updateCommand) {
        ReservationTime time = reservationTimeReference.getReservationTime(updateCommand.timeId());
        Reservation targetReservation = reservationRepository.findById(updateCommand.id())
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, updateCommand.id()));

        reservationValidator.validateAlreadyReservationExcludingSelf(updateCommand, targetReservation);
        Reservation updateReservation = targetReservation.update(
                updateCommand.name(),
                updateCommand.date(),
                time
        );
        reservationRepository.updateSchedule(updateReservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id));
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelReservation(Long id, String name) {
        Reservation targetReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id));
        targetReservation.cancel(name);
        reservationRepository.deleteByIdAndName(id, name);
    }
}
