package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.global.exception.WaitingErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Waiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final int sequence;

    private Waiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme, int sequence) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.sequence = sequence;
    }

    public static Waiting create(String name, LocalDate date, ReservationTime time, Theme theme, int sequence) {
        validateCreatableDateTime(date, time);
        return new Waiting(null, name, date, time, theme, sequence);
    }

    public static Waiting createRow(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            int sequence
    ) {
        return new Waiting(id, name, date, time, theme, sequence);
    }

    public Waiting appendId(Long id) {
        return new Waiting(id, name, date, time, theme, sequence);
    }

    public void cancel(String name) {
        validateOwner(name);
        validateModifiable();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public int getSequence() {
        return sequence;
    }

    private static void validateCreatableDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(WaitingErrorCode.WAITING_CREATE_IN_PAST);
        }
    }

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new BusinessException(WaitingErrorCode.WAITING_OWNER_MISMATCH);
        }
    }

    private void validateModifiable() {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(WaitingErrorCode.WAITING_CREATE_IN_PAST);
        }
        if (date.isEqual(LocalDate.now()) && time.getStartAt().isBefore(LocalTime.now())) {
            throw new BusinessException(WaitingErrorCode.WAITING_CREATE_IN_PAST);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            return false;
        }
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return System.identityHashCode(this);
    }
}
