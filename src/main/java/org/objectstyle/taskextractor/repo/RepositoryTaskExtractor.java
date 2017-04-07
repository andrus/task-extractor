package org.objectstyle.taskextractor.repo;

import org.objectstyle.taskextractor.Commit;

import java.time.LocalDate;
import java.util.Collection;

public interface RepositoryTaskExtractor {

    Collection<Commit> extract(LocalDate from, LocalDate to);
}
