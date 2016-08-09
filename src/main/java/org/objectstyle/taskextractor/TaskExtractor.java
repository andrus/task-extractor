package org.objectstyle.taskextractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class TaskExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExtractor.class);

    private String user;
    private List<String> repositories;
    private WebTarget apiBase;

    public TaskExtractor(WebTarget apiBase, String user, List<String> repositories) {
        this.user = user;
        this.apiBase = apiBase;
        this.repositories = repositories;
    }

    public Collection<Commit> extract() {

        Collection<Commit> commits = new ArrayList<>();

        Predicate<Commit> filter = user != null ? c -> user.equals(c.getUser()) : c -> true;

        repositories.forEach(r -> {
            String uri = "/repos/" + r + "/commits";

            LOGGER.info("read commits from repo: " + uri);

            GenericType<Collection<Commit>> type = new GenericType<Collection<Commit>>() {
            };
            Response response = apiBase.path(uri).request().get();
            try {
                Collection<Commit> singleRepoCommits = response.readEntity(type);
                singleRepoCommits.stream().filter(filter).forEach(c -> commits.add(c));
            } finally {
                response.close();
            }
        });

        return commits;
    }


}
