package taskRecr.atipera.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import taskRecr.atipera.exceptions.ResourceNotFoundException;
import taskRecr.atipera.model.ErrorResponse;
import taskRecr.atipera.model.Repository;
import taskRecr.atipera.services.ResponseFilterService;

import java.util.List;

@RestController
@RequestMapping("/github")
@CrossOrigin
public class GitHubRepController {
    @Value("${github.api.base.url}")
    private String GITHUB_API_BASE_URL;

    @Value("${github.api.pat}")
    private String PAT;

    @Autowired
    ResponseFilterService responseFilterService;

    @GetMapping("/repos/{username}")
    public ResponseEntity<?> getRepositories(@PathVariable String username){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + PAT);
        String url = GITHUB_API_BASE_URL + "/users/" + username + "/repos";
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Repository>> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            List<Repository> repositories = responseFilterService.getFilteredRepositories(responseEntity.getBody(),
                    GITHUB_API_BASE_URL);
            return ResponseEntity.ok().body(repositories);
        } catch (HttpClientErrorException userNotFoundEx) {
            System.out.println("Error Response: " + userNotFoundEx.getResponseBodyAsString());
            throw new ResourceNotFoundException(HttpStatus.NOT_FOUND.value(), "User not found");
        }
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
