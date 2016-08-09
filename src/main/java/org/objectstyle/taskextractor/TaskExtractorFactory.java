package org.objectstyle.taskextractor;

import io.bootique.jersey.client.HttpClientFactory;

import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.Objects;

public class TaskExtractorFactory {

    private static final String BASE_URL = "https://api.github.com";

    private String user;
    private List<String> repositories;

    public void setUser(String user) {
        this.user = user;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public TaskExtractor createExtractor(HttpClientFactory clientFactory) {
        WebTarget github = clientFactory.newClient().target(BASE_URL);
        return new TaskExtractor(github, Objects.requireNonNull(user), repositories);
    }
}
