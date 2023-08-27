package taskRecr.atipera.model;

import lombok.Data;

@Data
public class BranchInfo {
    private String branchName;
    private String lastCommitSha;

    public BranchInfo(String branchName, String lastCommitSha) {
        this.branchName = branchName;
        this.lastCommitSha = lastCommitSha;
    }
}
