package roomescape.waiting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    Optional<Waiting> findByReservationIdAndName(Long reservationId, String name);

    Optional<Waiting> findByDateAndTimeIdAndThemeIdAndName(
            LocalDate date,
            Long timeId,
            Long themeId,
            String name
    );

    int countByReservationId(Long reservationId);

    int countByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByName(String name);

    void deleteByIdAndName(Long id, String name);
}
