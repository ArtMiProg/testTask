package taskRecr.atipera.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import taskRecr.atipera.exceptions.ResourceNotFoundException;
import taskRecr.atipera.model.ErrorResponse;
import taskRecr.atipera.model.Repository;
import taskRecr.atipera.services.ResponseFilterService;
import java.util.List;

@RestController
@RequestMapping("/github")
@CrossOrigin
public class GitHubRepController {
    @Value("${github.api.pat}")
    private String PAT;
    @Value("${github.api.base.url}")
    private final String githubApiBaseUrl;
    private final ResponseFilterService responseFilterService;
    private final WebClient webClient;

    public GitHubRepController(ResponseFilterService responseFilterService, WebClient.Builder webClientBuilder,
                               @Value("${github.api.base.url}") String githubApiBaseUrl) {
        this.responseFilterService = responseFilterService;
        this.webClient = webClientBuilder.baseUrl(githubApiBaseUrl).build();
        this.githubApiBaseUrl = githubApiBaseUrl;
    }

    @GetMapping("/repos/{username}")
    public List<Repository> getRepositories(@PathVariable String username){
        String url = "/users/" + username + "/repos";
        Mono<List<Repository>> repositoryMono = webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + PAT)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Repository>>() {})
                .onErrorResume(WebClientResponseException.NotFound.class, notFoundException -> Mono.empty());
        List<Repository> repositories = repositoryMono.block();
        if (repositories == null) {
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND.value(), "User not found");
        }
        return responseFilterService.getFilteredRepositories(repositories, githubApiBaseUrl);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        var errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        var exceptionHeaders = new HttpHeaders();
        exceptionHeaders.setContentType(MediaType.APPLICATION_JSON);
        var errorResponse = new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).headers(exceptionHeaders).body(errorResponse);
    }
}
