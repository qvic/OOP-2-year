package models;

import org.eclipse.jgit.api.Git;

public class GitWrapper {
    private Git git;
    private String name;

    public GitWrapper(Git git, String name) {
        this.git = git;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Git getGit() {
        return git;
    }
}
