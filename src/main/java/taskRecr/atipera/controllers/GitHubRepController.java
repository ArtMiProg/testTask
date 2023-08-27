package taskRecr.atipera.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import taskRecr.atipera.exception.ResourceNotFoundException;
import taskRecr.atipera.model.Branch;
import taskRecr.atipera.model.BranchInfo;
import taskRecr.atipera.model.ErrorResponse;
import taskRecr.atipera.model.Repository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/github")
@CrossOrigin
public class GitHubRepController {
    @Value("${github.api.base.url}")
    private String GITHUB_API_BASE_URL;

    @Value("${github.api.pat}")
    private String PAT;

    @GetMapping("/repos/{username}")
    public ResponseEntity<?> getRepositories(@PathVariable String username,
                                             @RequestHeader(name = "Accept") String acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        String url = GITHUB_API_BASE_URL + "/users/" + username + "/repos";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Repository>> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            List<Repository> repositories = getFilteredRepositories(responseEntity.getBody());
            return ResponseEntity.ok().body(repositories);
        } catch (HttpClientErrorException userNotFoundEx) {
            System.out.println("Error Response: " + userNotFoundEx.getResponseBodyAsString());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND.value(), "User not found");
        }
    }
    private List<Repository> getFilteredRepositories(List<Repository> repositories) {
        return repositories.stream()
                .filter(repo -> !repo.isFork())
                .map(repo -> {
                    List<BranchInfo> branches = getBranchesForRepo(repo.getOwner().getLogin(), repo.getName());
                    return new Repository(repo.getName(), repo.isFork(), repo.getOwner(), branches);
                })
                .collect(Collectors.toList());
    }
    private List<BranchInfo> getBranchesForRepo(String username, String repoName) {
        String url = GITHUB_API_BASE_URL + "/repos/" + username + "/" + repoName + "/branches";
        ResponseEntity<List<Branch>> responseEntity = new RestTemplate().exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY,
                new ParameterizedTypeReference<List<Branch>>() {}
        );
        return responseEntity.getBody().stream()
                .map(branch -> new BranchInfo(branch.getName(), branch.getCommit().getSha()))
                .collect(Collectors.toList());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        HttpHeaders exceptionHeaders = new HttpHeaders();
        exceptionHeaders.setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).headers(exceptionHeaders).body(errorResponse);
    }
}
