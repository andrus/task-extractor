package org.objectstyle.taskextractor.github;

import org.objectstyle.taskextractor.Commit;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

        return new GithubExtractorWorker(user, repositories, apiBase, from, to).extractCommits();
    }
}
