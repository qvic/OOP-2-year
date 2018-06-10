package models;

import org.eclipse.jgit.api.Git;

import java.util.Optional;

public class GitWrapper {
    private Git git;
    private String name;

    public GitWrapper(Git git, String name) {
        this.git = git;
        this.name = name;
    }

    public GitWrapper(String name) {
        this.git = null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<Git> getGit() {
        return Optional.ofNullable(git);
    }
}
