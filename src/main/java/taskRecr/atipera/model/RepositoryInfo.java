package taskRecr.atipera.model;

import lombok.Data;

import java.util.List;
@Data
public class RepositoryInfo {
    private String repositoryName;
    private String ownerLogin;
    private List<BranchInfo> branches;

    public RepositoryInfo(String repositoryName, String ownerLogin, List<BranchInfo> branches) {
        this.repositoryName = repositoryName;
        this.ownerLogin = ownerLogin;
        this.branches = branches;
    }
}
