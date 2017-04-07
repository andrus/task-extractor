package org.objectstyle.taskextractor;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jersey.client.HttpClientFactory;

import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Objects;

@BQConfig("Specifies task extraction parameters, such as code repositories, the user to look for, etc.")
public class TaskExtractorFactory {

    private static final String BASE_URL = "https://api.github.com";

    private String user;
    private List<String> repositories;

    @BQConfigProperty
    public void setUser(String user) {
        this.user = user;
    }

    @BQConfigProperty
    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public TaskExtractor createExtractor(HttpClientFactory clientFactory) {

        WebTarget github = clientFactory.newAuthenticatedClient("github").target(BASE_URL);
        return new TaskExtractor(github, Objects.requireNonNull(user), repositories);
    }
}
