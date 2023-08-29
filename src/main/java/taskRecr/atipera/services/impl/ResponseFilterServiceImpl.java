package taskRecr.atipera.services.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import taskRecr.atipera.model.Branch;
import taskRecr.atipera.model.BranchInfo;
import taskRecr.atipera.model.Repository;
import taskRecr.atipera.services.ResponseFilterService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResponseFilterServiceImpl implements ResponseFilterService {
    @Override
    public List<Repository> getFilteredRepositories(List<Repository> repositories, String BASE_URL) {
        return repositories.stream()
                .filter(repo -> !repo.isFork())
                .map(repo -> {
                    var branches = getBranchesForRepo(BASE_URL, repo.getOwner().getLogin(), repo.getName());
                    return new Repository(repo.getName(), repo.isFork(), repo.getOwner(), branches);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BranchInfo> getBranchesForRepo(String BASE_URL, String username, String repoName) {
        var url = BASE_URL + "/repos/" + username + "/" + repoName + "/branches";
        var responseEntity = new RestTemplate().exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Branch>>() {}
        );
        return responseEntity.getBody().stream()
                .map(branch -> new BranchInfo(branch.getName(), branch.getCommit().getSha()))
                .collect(Collectors.toList());
    }
}
