package org.objectstyle.taskextractor.git;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.di.Injector;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@JsonTypeName("git")
@BQConfig("Configurable task extractor for a set of local git repos.")
public class GitExtractorFactory implements RepositoryTaskExtractorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitExtractorFactory.class);

    private List<File> repositories;
    private boolean fetch;
    private String user;

    @Override
    public RepositoryTaskExtractor createExtractor(Injector injector) {

        List<File> validRepos = getRepositories().stream()
                .filter(GitExtractorFactory::validRepoDirectory)
                .map(f -> new File(f, ".git"))
                .filter(GitExtractorFactory::validRepoDirectory)
                .collect(toList());

        return new GitExtractor(validRepos, user, fetch);
    }

    @BQConfigProperty
    public void setRepositories(List<File> repositories) {
        this.repositories = repositories;
    }

    @BQConfigProperty
    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    @BQConfigProperty
    public void setUser(String user) {
        this.user = user;
    }

    private List<File> getRepositories() {
        return repositories != null ? repositories : Collections.emptyList();
    }

    private static boolean validRepoDirectory(File file) {

        if (!file.exists()) {
            LOGGER.warn("No such repository, skipping: " + file.getName());
            return false;
        }

        if (!file.isDirectory()) {
            LOGGER.warn("Repository is not a directory, skipping: " + file.getName());
            return false;
        }

        if (!file.canRead()) {
            LOGGER.warn("Repository is not accessible, skipping: " + file.getName());
            return false;
        }

        return true;
    }
}
