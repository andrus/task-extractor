package org.objectstyle.taskextractor.github;

import com.nhl.dflib.DataFrame;
import com.nhl.dflib.filter.ValuePredicate;
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

class GithubExtractorWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubExtractorWorker.class);

    private LocalDate from;
    private LocalDate to;
    private List<String> repositories;
    private WebTarget apiBase;
    private ValuePredicate<String> userMatches;
    private GenericType<Collection<Branch>> branchType;


    public GithubExtractorWorker(
            String user,
            List<String> repositories,
            WebTarget apiBase,
            LocalDate from,
            LocalDate to) {

        this.from = from;
        this.to = to;
        this.repositories = repositories;
        this.apiBase = apiBase;

        this.userMatches = user != null ? Commit.userMatches(user) : c -> true;
        this.branchType = new GenericType<Collection<Branch>>() {
        };
    }

    public DataFrame extractCommits() {
        // TODO: parallel extraction...
        return repositories.stream()
                .map(this::extractRepoCommits)
                // TODO: replace "reduce" with Collector that can batch-concat multiple frames
                .reduce(DataFrame::vConcat)
                .orElseGet(() -> DataFrame.forRows(Commit.index()));
    }

    private DataFrame extractRepoCommits(String repository) {
        return readBranches(repository)
                .stream()
                .map(b -> extractBranchCommits(repository, b))
                .reduce(DataFrame::vConcat)
                .orElseGet(() -> DataFrame.forRows(Commit.index()))
                // dedupe matching commits from multiple branches
                .group(Commit.HASH.ordinal()).head(1)
                .toDataFrame();
    }

    private DataFrame extractBranchCommits(String repository, Branch branch) {

        String uri = "/repos/" + repository + "/commits";

        LOGGER.info("read commits from repo and branch {}:{}", uri, branch.getName());

        try (Response response = apiBase.path(uri)
                .queryParam("since", from)
                .queryParam("until", to)
                .queryParam("sha", branch.getHash())
                .request()
                .get()) {

            if (response.getStatus() == 200) {
                return response
                        .readEntity(DataFrame.class)
                        .filter(Commit.USER.ordinal(), userMatches)
                        .convertColumn(Commit.REPO.ordinal(), v -> repository);

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
