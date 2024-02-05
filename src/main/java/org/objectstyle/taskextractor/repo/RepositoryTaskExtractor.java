package org.objectstyle.taskextractor.repo;

import org.dflib.DataFrame;

import java.time.LocalDate;

public interface RepositoryTaskExtractor {

    DataFrame extract(LocalDate from, LocalDate to);
}
