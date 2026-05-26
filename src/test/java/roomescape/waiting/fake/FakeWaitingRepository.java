package roomescape.waiting.fake;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

public class FakeWaitingRepository implements WaitingRepository {

    private final Map<Long, Waiting> store = new HashMap<>();
    private Long sequence = 1L;

    @Override
    public Waiting save(Waiting waiting) {
        if (waiting.getId() == null) {
            Waiting saved = Waiting.createRow(
                    sequence++,
                    waiting.getReservationId(),
                    waiting.getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme(),
                    waiting.getSequence()
            );
            store.put(saved.getId(), saved);
            return saved;
        }

        store.put(waiting.getId(), waiting);
        return waiting;
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Waiting> findByReservationIdAndName(Long reservationId, String name) {
        return store.values().stream()
                .filter(waiting -> waiting.getReservationId().equals(reservationId))
                .filter(waiting -> waiting.getName().equals(name))
                .findFirst();
    }

    @Override
    public Optional<Waiting> findByDateAndTimeIdAndThemeIdAndName(
            LocalDate date,
            Long timeId,
            Long themeId,
            String name
    ) {
        return store.values().stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .filter(waiting -> waiting.getName().equals(name))
                .findFirst();
    }

    @Override
    public int countByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return (int) store.values().stream()
                .filter(waiting -> waiting.getDate().equals(date))
                .filter(waiting -> waiting.getTime().getId().equals(timeId))
                .filter(waiting -> waiting.getTheme().getId().equals(themeId))
                .count();
    }

    @Override
    public int countByReservationId(Long reservationId) {
        return (int) store.values().stream()
                .filter(waiting -> waiting.getReservationId().equals(reservationId))
                .count();
    }

    @Override
    public List<Waiting> findByName(String name) {
        return store.values().stream()
                .filter(waiting -> waiting.getName().equals(name))
                .sorted(Comparator.comparing(Waiting::getId))
                .toList();
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        store.values().removeIf(waiting ->
                waiting.getId().equals(id)
                        && waiting.getName().equals(name)
        );
    }
}
