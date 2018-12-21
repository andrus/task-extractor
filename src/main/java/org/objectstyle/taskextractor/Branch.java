package org.objectstyle.taskextractor;

public class Branch {

    private String name;
    private String hash;

    public Branch(String name, String hash) {
        this.name = name;
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }
}
