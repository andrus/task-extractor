package org.objectstyle.taskextractor;

import com.nhl.dflib.Index;
import com.nhl.dflib.filter.ValuePredicate;
import com.nhl.dflib.map.ValueMapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public enum Commit {

    TIME, REPO, MESSAGE, USER, HASH;

    public static Index index() {
        return Index.forLabels(Commit.class);
    }

    public static ValuePredicate<ZonedDateTime> timeBetween(LocalDate from, LocalDate to) {
        ZonedDateTime fromDT = Objects.requireNonNull(from).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime toDT = Objects.requireNonNull(to).plusDays(1).atStartOfDay(ZoneOffset.UTC);
        return (ZonedDateTime t) -> !(t.isBefore(fromDT) || t.isAfter(toDT));
    }

    public static ValuePredicate<String> userMatches(String user) {
        return (String u) -> Objects.equals(u, user);
    }

    public static ValueMapper<String, String> trimMessage() {
        return m -> m != null ? m.split("\\r?\\n")[0].trim() : null;
    }
}
