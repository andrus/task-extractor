package org.objectstyle.taskextractor;

import io.bootique.jersey.client.HttpClientFactory;

import javax.ws.rs.client.WebTarget;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class TaskExtractorFactory {

    private static final String BASE_URL = "https://api.github.com";

    private String user;
    private List<String> repositories;
    private LocalDate from;
    private LocalDate to;

    public void setUser(String user) {
        this.user = user;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public void setFrom(String from) {
        this.from = LocalDate.parse(Objects.requireNonNull(from));
    }

    public void setTo(String to) {
        this.to = LocalDate.parse(Objects.requireNonNull(to));
    }

    public TaskExtractor createExtractor(HttpClientFactory clientFactory) {

        // TODO: use last calendar month as from/to range if not set

        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        if (from.compareTo(to) >= 0) {
            throw new IllegalStateException("From date must be before to date: " + from + ".." + to);
        }

        WebTarget github = clientFactory.newAuthenticatedClient("github").target(BASE_URL);
        return new TaskExtractor(github,
                Objects.requireNonNull(user),
                repositories,
                Objects.requireNonNull(from),
                Objects.requireNonNull(to));
    }
}
