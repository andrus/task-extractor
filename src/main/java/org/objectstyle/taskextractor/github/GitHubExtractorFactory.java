package org.objectstyle.taskextractor.github;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.HttpClientFactory;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractorFactory;

import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Objects;

@JsonTypeName("github")
@BQConfig("Configurable task extractor for a set of GitHub repos.")
public class GitHubExtractorFactory implements RepositoryTaskExtractorFactory {

    private static final String BASE_URL = "https://api.github.com";

    private List<String> repositories;
    private String user;

    @BQConfigProperty
    public void setRepositories(List<String> paths) {
        this.repositories = paths;
    }

    @BQConfigProperty
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public RepositoryTaskExtractor createExtractor(Injector injector) {
        WebTarget github = injector.getInstance(HttpClientFactory.class).newBuilder().auth("github").build().target(BASE_URL);
        return new GithubExtractor(github, Objects.requireNonNull(user), repositories);
    }
}
