package roomescape.waiting.domain;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    Optional<Waiting> findByReservationIdAndName(Long reservationId, String name);

    int countByReservationId(Long reservationId);

    List<Waiting> findByName(String name);

    void deleteByIdAndName(Long id, String name);
}
