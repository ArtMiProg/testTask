package taskRecr.atipera.model;

import lombok.Data;

import java.util.List;

@Data
public class Repository {
    private String name;
    private boolean fork;
    private Owner owner;
    private List<BranchInfo> branches;

    public Repository() {
    }

    public Repository(String name, boolean fork, Owner owner, List<BranchInfo> branches) {
        this.name = name;
        this.fork = fork;
        this.owner = owner;
        this.branches = branches;
    }
}