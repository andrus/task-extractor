package org.objectstyle.taskextractor.github;

import org.objectstyle.taskextractor.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

class GithubExtractorWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubExtractorWorker.class);

    private LocalDate from;
    private LocalDate to;
    private String user;
    private List<String> repositories;
    private WebTarget apiBase;
    private Predicate<Commit> filter;


    public GithubExtractorWorker(
            String user,
            List<String> repositories,
            WebTarget apiBase,
            LocalDate from,
            LocalDate to) {

        this.from = from;
        this.to = to;
        this.user = user;
        this.repositories = repositories;
        this.apiBase = apiBase;
        this.filter = user != null ? c -> user.equals(c.getUser()) : c -> true;
    }

    public Collection<Commit> extractCommits() {
        Collection<Commit> commits = new ArrayList<>();
        // TODO: parallel extraction...
        repositories.forEach(r -> commits.addAll(extractCommits(r)));
        return commits;
    }

    private Collection<Commit> extractCommits(String repository) {

        Collection<Commit> commits = new ArrayList<>();

        String uri = "/repos/" + repository + "/commits";

        LOGGER.info("read commits from the repo {}", uri);

        GenericType<Collection<Commit>> type = new GenericType<Collection<Commit>>() {
        };

        try (Response response = apiBase.path(uri)
                .queryParam("since", from)
                .queryParam("until", to)
                .request()
                .get()) {

            if (response.getStatus() == 200) {

                Collection<Commit> singleRepoCommits = response.readEntity(type);
                singleRepoCommits.stream().filter(filter).forEach(c -> {

                    // TODO: mutating object that we did not create

                    c.setRepo(repository);
                    commits.add(c);
                });
            } else {
                throw new IllegalStateException("Bad response from Github: " + response.getStatus());
            }
        }

        return commits;
    }

}
