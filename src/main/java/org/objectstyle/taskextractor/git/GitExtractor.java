package org.objectstyle.taskextractor.git;

import org.dflib.DataFrame;
import org.dflib.Extractor;
import org.dflib.JoinType;
import org.dflib.RowPredicate;
import org.dflib.concat.VConcat;
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
import java.util.function.Predicate;

public class GitExtractor implements RepositoryTaskExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitExtractor.class);

    private final Collection<File> repositories;
    private final boolean fetch;
    private final String user;

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
        return DataFrame
                .byRow(
                        Extractor.$col(rc -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(rc.getCommitTime()), ZoneOffset.UTC)),
                        Extractor.$col(rc -> repoName),
                        Extractor.$col(RevCommit::getShortMessage),
                        Extractor.$col(rc -> rc.getAuthorIdent().getName()),
                        Extractor.$col(RevCommit::getName)
                )
                .columnIndex(Commit.index())
                .ofIterable(commits);
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

        Predicate<String> userPredicate = Commit.userMatches(user);
        Predicate<ZonedDateTime> timePredicate = Commit.timeBetween(from, to);

        RowPredicate prefilter = RowPredicate
                .of(Commit.TIME.ordinal(), timePredicate::test)
                .and(Commit.USER.ordinal(), userPredicate::test);

        // TODO: parallel extraction... just change to parallel stream
        DataFrame[] perRepoCommits = repositories.stream()
                .peek(d -> LOGGER.info("read commits from the local repo {}", d))
                .map(GitExtractor::repository)
                .map(r -> readRepo(r, prefilter))
                .toArray(DataFrame[]::new);

        return perRepoCommits.length > 0
                ? VConcat.concat(JoinType.left, perRepoCommits)
                : DataFrame.empty(Commit.index());
    }

    private DataFrame readRepo(Git r, RowPredicate prefilter) {
        fetch(r);
        String repoName = r.getRepository().getDirectory().getParentFile().getName();
        return collectCommits(repoName, allCommits(r)).rows(prefilter).select();
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
