package org.objectstyle.taskextractor;

import java.time.ZonedDateTime;

public class Commit {

    private ZonedDateTime time;
    private String message;
    private String hash;
    private String repo;
    private String user;

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFirstMessageLine() {
        if (message == null) {
            return null;
        }

        // TODO: not particularly efficient
        return message.split("\\r?\\n")[0].trim();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return repo + " : " + user + " " + hash + " " + time + " " + getFirstMessageLine();
    }
}
