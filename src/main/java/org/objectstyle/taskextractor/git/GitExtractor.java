package org.objectstyle.taskextractor.git;

import com.nhl.dflib.DataFrame;
import com.nhl.dflib.filter.RowPredicate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.objectstyle.taskextractor.Commit;
import org.objectstyle.taskextractor.repo.RepositoryTaskExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;

public class GitExtractor implements RepositoryTaskExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitExtractor.class);

    private Collection<File> repositories;
    private boolean fetch;
    private String user;

    public GitExtractor(Collection<File> repositories, String user, boolean fetch) {
        this.repositories = repositories;
        this.fetch = fetch;
        this.user = user;
    }

    private static Git repository(File repoDir) {
        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(repoDir)
                    .setMustExist(true)
                    .build();
            return new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException("Error creating repo object for " + repoDir, e);
        }
    }

    public static DataFrame collectCommits(String repoName, Iterable<RevCommit> commits) {
        return DataFrame.forObjects(
                Commit.index(),
                commits,
                rc -> GitExtractor.toCommit(repoName, rc));
    }

    private static Object[] toCommit(String repoName, RevCommit rc) {
        Instant i = Instant.ofEpochSecond(rc.getCommitTime());
        ZonedDateTime rcTime = ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
        return DataFrame.row(rcTime, repoName, rc.getShortMessage(), rc.getAuthorIdent().getName(), rc.getName());
    }

    private static Iterable<RevCommit> allCommits(Git repository) {
        try {
            return repository
                    .log()
                    .all()
                    .call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Error listing repo revisions", e);
        }
    }

    @Override
    public DataFrame extract(LocalDate from, LocalDate to) {

        LOGGER.info("find commits between {} and {}", from, to);

        RowPredicate prefilter = RowPredicate
                .forColumn(Commit.TIME.ordinal(), Commit.timeBetween(from, to))
                .and(Commit.USER.ordinal(), Commit.userMatches(user));


        // TODO: parallel extraction... just change to parallel stream
        return repositories.stream()
                .peek(d -> LOGGER.info("read commits from the local repo {}", d))
                .map(GitExtractor::repository)
                .map(r -> readRepo(r, prefilter))
                // TODO: replace "reduce" with Collector that can batch-concat multiple frames
                .reduce(DataFrame::vConcat)
                .orElseGet(() -> DataFrame.forRows(Commit.index()));
    }

    private DataFrame readRepo(Git r, RowPredicate prefilter) {
        fetch(r);
        String repoName = r.getRepository().getDirectory().getParentFile().getName();
        return collectCommits(repoName, allCommits(r)).filter(prefilter);
    }

    private void fetch(Git repo) {
        if (fetch) {

            LOGGER.info("fetch {}", repo.getRepository().getDirectory().getParentFile().getName());
            try {
                repo.fetch().setRemote("origin").call();
            } catch (GitAPIException e) {
                throw new RuntimeException("Error fetching repo", e);
            }
        }
    }
}
