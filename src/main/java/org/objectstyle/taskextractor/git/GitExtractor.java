package org.objectstyle.taskextractor.git;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

    @Override
    public Collection<Commit> extract(LocalDate from, LocalDate to) {

        ZonedDateTime fromDT = Objects.requireNonNull(from).atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime toDT = Objects.requireNonNull(to).plusDays(1).atStartOfDay(ZoneOffset.UTC);

        LOGGER.info("find commits between {} and {}", from, to);

        Collection<Commit> commits = new ArrayList<>();

        // TODO: parallel extraction...
        repositories.stream()
                .peek(d -> LOGGER.info("read commits from the local repo {}", d))
                .map(GitExtractor::repository)
                .forEach(r -> {

                            String repoName = r.getRepository().getDirectory().getParentFile().getName();

                            for (RevCommit rc : masterCommits(r)) {
                                Instant i = Instant.ofEpochSecond(rc.getCommitTime());
                                ZonedDateTime rcTime = ZonedDateTime.ofInstant(i, ZoneOffset.UTC);

                                // commits are coming in reverse order so newer dates need to be skipped, older dates
                                // indicate that we are done with our range...

                                if (rcTime.isBefore(fromDT)) {
                                    break;
                                }

                                if (rcTime.isAfter(toDT)) {
                                    continue;
                                }

                                if (!user.equals(rc.getAuthorIdent().getName())) {
                                    continue;
                                }

                                commits.add(toCommit(repoName, rc, rcTime));
                            }
                        }
                );

        return commits;
    }

    private Commit toCommit(String repoName, RevCommit revCommit, ZonedDateTime revCommitTime) {
        Commit commit = new Commit();
        commit.setTime(revCommitTime);
        commit.setMessage(revCommit.getShortMessage());
        commit.setUser(revCommit.getAuthorIdent().getName());
        commit.setHash(revCommit.getName());
        commit.setRepo(repoName);
        return commit;
    }

    private Iterable<RevCommit> masterCommits(Git repository) {
        try {
            return repository
                    .log()
                    // TODO: other branches?
                    .add(repository.getRepository().resolve("origin/master"))
                    .call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Error listing repo revisions", e);
        }
    }


    private static Git repository(File repoDir) {
        Repository repository;
        try {
            repository = new FileRepositoryBuilder()
                    .setGitDir(repoDir)
                    .setMustExist(true)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Error creating repo object for " + repoDir, e);
        }

        return new Git(repository);
    }
}
