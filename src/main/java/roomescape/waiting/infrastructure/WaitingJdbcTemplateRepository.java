package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Repository
public class WaitingJdbcTemplateRepository implements WaitingRepository {

    private static final String WAITING_SELECT_QUERY = """
        SELECT ranked.id,
               ranked.reservation_id,
               ranked.name AS waiting_name,
               ranked.date,
               ranked.time_id,
               ranked.start_at,
               ranked.theme_id,
               ranked.theme_name,
               ranked.theme_description,
               ranked.thumbnail_url,
               ranked.sequence
        FROM (
            SELECT w.id,
                   w.reservation_id,
                   w.name,
                   w.date,
                   rt.id AS time_id,
                   rt.start_at,
                   t.id AS theme_id,
                   t.name AS theme_name,
                   t.description AS theme_description,
                   t.thumbnail_url,
                   ROW_NUMBER() OVER (
                       PARTITION BY w.date, w.time_id, w.theme_id
                       ORDER BY w.id
                   ) AS sequence
            FROM waiting w
            JOIN reservation_time rt ON w.time_id = rt.id
            JOIN theme t ON w.theme_id = t.id
        ) ranked
        """;
    private static final String FIND_BY_ID_QUERY = WAITING_SELECT_QUERY + "WHERE ranked.id = ?";
    private static final String FIND_BY_RESERVATION_ID_AND_NAME_QUERY = WAITING_SELECT_QUERY + """
        WHERE ranked.reservation_id = ?
          AND ranked.name = ?
        """;
    private static final String FIND_BY_NAME_QUERY = WAITING_SELECT_QUERY + """
        WHERE ranked.name = ?
        ORDER BY ranked.id
        """;
    private static final String FIND_BY_SLOT_AND_NAME_QUERY = WAITING_SELECT_QUERY + """
        WHERE ranked.date = ?
          AND ranked.time_id = ?
          AND ranked.theme_id = ?
          AND ranked.name = ?
        """;
    private static final String COUNT_BY_SLOT_QUERY = """
        SELECT COUNT(*)
        FROM waiting
        WHERE date = ?
          AND time_id = ?
          AND theme_id = ?
        """;
    private static final String COUNT_BY_RESERVATION_ID_QUERY = """
        SELECT COUNT(*)
        FROM waiting
        WHERE reservation_id = ?
        """;
    private static final String DELETE_BY_ID_AND_NAME_QUERY = "DELETE FROM waiting WHERE id = ? AND name = ?";
    private static final RowMapper<Waiting> ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = ReservationTime.createRow(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );

        Theme theme = Theme.createRow(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("theme_description"),
                rs.getString("thumbnail_url")
        );

        return Waiting.createRow(
                rs.getLong("id"),
                rs.getLong("reservation_id"),
                rs.getString("waiting_name"),
                rs.getDate("date").toLocalDate(),
                time,
                theme,
                rs.getInt("sequence")
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingJdbcTemplateRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Waiting save(Waiting waiting) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", waiting.getName());
        params.put("reservation_id", waiting.getReservationId());
        params.put("date", waiting.getDate());
        params.put("time_id", waiting.getTime().getId());
        params.put("theme_id", waiting.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return waiting.appendId(id);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        List<Waiting> waitings = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                ROW_MAPPER,
                id
        );
        return waitings.stream()
                .findFirst();
    }

    @Override
    public Optional<Waiting> findByReservationIdAndName(Long reservationId, String name) {
        List<Waiting> waitings = jdbcTemplate.query(
                FIND_BY_RESERVATION_ID_AND_NAME_QUERY,
                ROW_MAPPER,
                reservationId,
                name
        );
        return waitings.stream()
                .findFirst();
    }

    @Override
    public Optional<Waiting> findByDateAndTimeIdAndThemeIdAndName(
            LocalDate date,
            Long timeId,
            Long themeId,
            String name
    ) {
        List<Waiting> waitings = jdbcTemplate.query(
                FIND_BY_SLOT_AND_NAME_QUERY,
                ROW_MAPPER,
                date,
                timeId,
                themeId,
                name
        );
        return waitings.stream()
                .findFirst();
    }

    @Override
    public int countByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_BY_SLOT_QUERY,
                Integer.class,
                date,
                timeId,
                themeId
        );
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public int countByReservationId(Long reservationId) {
        Integer count = jdbcTemplate.queryForObject(
                COUNT_BY_RESERVATION_ID_QUERY,
                Integer.class,
                reservationId
        );
        if (count == null) {
            return 0;
        }
        return count;
    }

    @Override
    public List<Waiting> findByName(String name) {
        return jdbcTemplate.query(
                FIND_BY_NAME_QUERY,
                ROW_MAPPER,
                name
        );
    }

    @Override
    public void deleteByIdAndName(Long id, String name) {
        jdbcTemplate.update(
                DELETE_BY_ID_AND_NAME_QUERY,
                id,
                name
        );
    }
}
