package lat.luisdias.stock_app_main_service.stock.infra.util;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimestampUtil {
    private static final DateTimeFormatter formatterWithTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter formatterWithoutTime = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String toStringWithTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(formatterWithTime);
    }

    public static String toStringWithoutTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().format(formatterWithoutTime);
    }

    public static Timestamp toTimestampFromLocalDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Timestamp.from(OffsetDateTime.parse(value).toInstant());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(I18n.get("validation.date.invalid"));
        }
    }
}
