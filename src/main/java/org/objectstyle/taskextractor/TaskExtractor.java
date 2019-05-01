package org.objectstyle.taskextractor;

import com.nhl.dflib.DataFrame;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;

import java.time.LocalDate;
import java.util.Collection;

public class TaskExtractor {

    private Collection<RepositoryTaskExtractor> extractors;

    public TaskExtractor(Collection<RepositoryTaskExtractor> extractors) {
        this.extractors = extractors;
    }

    public DataFrame extract(LocalDate from, LocalDate to) {
        return extractors.stream().map(e -> e.extract(from, to)).reduce(DataFrame::vConcat).get();
    }
}
