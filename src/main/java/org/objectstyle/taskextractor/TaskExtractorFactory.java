package org.objectstyle.taskextractor;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractorFactory;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@BQConfig("Configures task extractors.")
public class TaskExtractorFactory {

    private List<RepositoryTaskExtractorFactory> extractors;

    public TaskExtractorFactory() {
        this.extractors = Collections.emptyList();
    }

    @BQConfigProperty("A list of extractors for task repositories.")
    public void setExtractors(List<RepositoryTaskExtractorFactory> extractors) {
        this.extractors = extractors;
    }

    public TaskExtractor createExtractor() {
        List<RepositoryTaskExtractor> extractorList = extractors
                .stream()
                .map(e -> e.createExtractor())
                .collect(toList());
        return new TaskExtractor(extractorList);
    }
}
