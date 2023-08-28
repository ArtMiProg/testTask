package taskRecr.atipera.services;

import taskRecr.atipera.model.BranchInfo;
import taskRecr.atipera.model.Repository;

import java.util.List;

public interface ResponseFilterService {
    List<Repository> getFilteredRepositories(List<Repository> repositories,String BASE_URL);

    List<BranchInfo> getBranchesForRepo(String BASE_URL, String username, String repoName);
}
