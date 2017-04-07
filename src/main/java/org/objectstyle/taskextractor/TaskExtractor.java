package org.objectstyle.taskextractor;

import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Stream;

public class TaskExtractor {

    private Collection<RepositoryTaskExtractor> extractors;

    public TaskExtractor(Collection<RepositoryTaskExtractor> extractors) {
        this.extractors = extractors;
    }

    public Stream<Commit> extract(LocalDate from, LocalDate to) {
        return extractors.stream().map(e -> e.extract(from, to)).flatMap(Collection::stream);
    }
}
