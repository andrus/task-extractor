package org.objectstyle.taskextractor.github;

import org.objectstyle.taskextractor.Branch;
import org.objectstyle.taskextractor.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

class GithubExtractorWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubExtractorWorker.class);

    private LocalDate from;
    private LocalDate to;
    private String user;
    private List<String> repositories;
    private WebTarget apiBase;
    private Predicate<Commit> filter;
    private GenericType<Collection<Commit>> commitType;
    private GenericType<Collection<Branch>> branchType;


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
        this.commitType = new GenericType<Collection<Commit>>() {
        };
        this.branchType = new GenericType<Collection<Branch>>() {
        };
    }

    public Collection<Commit> extractCommits() {
        // TODO: parallel extraction...

        // de-dupe commits between the branches
        Map<String, Commit> commits = new ConcurrentHashMap<>();

        repositories.forEach(r -> extractCommits(commits, r));
        return commits.values();
    }

    private void extractCommits(Map<String, Commit> commits, String repository) {
        Collection<Branch> branches = readBranches(repository);
        branches.forEach(b -> extractCommits(commits, repository, b));
    }

    private void extractCommits(Map<String, Commit> commits, String repository, Branch branch) {

        String uri = "/repos/" + repository + "/commits";

        LOGGER.info("read commits from repo and branch {}:{}", uri, branch.getName());

        try (Response response = apiBase.path(uri)
                .queryParam("since", from)
                .queryParam("until", to)
                .queryParam("sha", branch.getHash())
                .request()
                .get()) {

            if (response.getStatus() == 200) {

                Collection<Commit> singleRepoCommits = response.readEntity(commitType);
                singleRepoCommits.stream().filter(filter).forEach(c -> {

                    // TODO: mutating object that we did not create

                    c.setRepo(repository);
                    commits.put(c.getHash(), c);
                });
            } else {
                throw new IllegalStateException("Bad response from Github: " + response.getStatus());
            }
        }
    }

    private Collection<Branch> readBranches(String repository) {

        String uri = "/repos/" + repository + "/branches";

        LOGGER.info("read branches from the repo {}", uri);

        try (Response response = apiBase.path(uri).request().get()) {

            if (response.getStatus() == 200) {
                return response.readEntity(branchType);
            } else {
                throw new IllegalStateException("Bad response from Github: " + response.getStatus());
            }
        }
    }

}
