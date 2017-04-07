package org.objectstyle.taskextractor.github;

import org.objectstyle.taskextractor.Commit;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class GithubExtractor implements RepositoryTaskExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubExtractor.class);

    private String user;
    private List<String> repositories;
    private WebTarget apiBase;

    public GithubExtractor(WebTarget apiBase, String user, List<String> repositories) {
        this.user = user;
        this.apiBase = apiBase;
        this.repositories = repositories;
    }

    public Collection<Commit> extract(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        LOGGER.info("find commits between {} and {}", from, to);

        Collection<Commit> commits = new ArrayList<>();

        Predicate<Commit> filter = user != null ? c -> user.equals(c.getUser()) : c -> true;

        // TODO: parallel extraction...
        repositories.forEach(r -> {
            String uri = "/repos/" + r + "/commits";

            LOGGER.info("read commits from the repo {}", uri);

            GenericType<Collection<Commit>> type = new GenericType<Collection<Commit>>() {
            };

            Response response = apiBase.path(uri)
                    .queryParam("since", from)
                    .queryParam("until", to)
                    .request().get();
            try {
                Collection<Commit> singleRepoCommits = response.readEntity(type);
                singleRepoCommits.stream().filter(filter).forEach(c -> {

                    // TODO: mutating object that we did not create

                    c.setRepo(r);
                    commits.add(c);
                });
            } finally {
                response.close();
            }
        });

        return commits;
    }
}
