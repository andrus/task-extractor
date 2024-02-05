package org.objectstyle.taskextractor;

import org.dflib.Index;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Commit {

    TIME, REPO, MESSAGE, USER, HASH;

    public static Index index() {
        return Index.of(Commit.class);
    }

    public static Predicate<ZonedDateTime> timeBetween(LocalDate from, LocalDate to) {
        ZonedDateTime fromDT = Objects.requireNonNull(from).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime toDT = Objects.requireNonNull(to).plusDays(1).atStartOfDay(ZoneOffset.UTC);
        return (ZonedDateTime t) -> !(t.isBefore(fromDT) || t.isAfter(toDT));
    }

    public static Predicate<String> userMatches(String user) {
        return (String u) -> Objects.equals(u, user);
    }

    public static Function<String, String> trimMessage() {
        return m -> m != null ? m.split("\\r?\\n")[0].trim() : null;
    }

    public static String weekend(ZonedDateTime zdt) {
        int d = zdt.getDayOfWeek().getValue();
        return d == 6 || d == 7 ? "weekend" : null;
    }
}
