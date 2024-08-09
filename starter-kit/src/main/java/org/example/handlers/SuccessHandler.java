package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.config.CommonConfig;
import org.example.dto.RequestType;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.utils.GitHubUtil;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
public class SuccessHandler extends Handler {

    private final CommonConfig commonConfig;


    public SuccessHandler(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public SuccessHandler(Handler nextHandler, CommonConfig commonConfig) {
        super(nextHandler);
        this.commonConfig = commonConfig;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if(request.getStatus() == StarterRequest.Status.SUCCESS) {

            if(request.getRequestType() == RequestType.LIBRARY) {

                // either create a JAR file and upload in common repository or create a docker image and publish to docker namespace.
                // update SQL db with docker-Link or repository Link.

            }
            else {
                createGitHubRelease(request.getOrganization(), request.getArtifactId(), "1.0.0", "1.0.0", "First release");

                //Update SQL DB with repository link and success status.
            }
        }

        super.handle(request);
    }

    public void createGitHubRelease(String owner, String repoName, String tagName, String releaseName, String body) {

        if(!releaseExists(owner, repoName)) {
            RestTemplate restTemplate = new RestTemplate();

            JSONObject json = new JSONObject();
            json.put("tag_name", tagName);
            json.put("name", releaseName);
            json.put("body", body);
            json.put("draft", false);
            json.put("prerelease", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "token " + commonConfig.getGitToken());

            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    String.format("%s/repos/%s/%s/releases", commonConfig.getGitApiUrl(), owner, repoName),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Repository updated successfully: " + response.getBody());
            } else {
                log.error("Failed to update repository: " + response.getBody());
            }
        }

    }

    public boolean releaseExists(String owner, String repoName) {
        String url = String.format("%s/repos/%s/%s/tags", commonConfig.getGitApiUrl(), owner, repoName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + commonConfig.getGitToken());
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = new RestTemplate().exchange(url, HttpMethod.GET, requestEntity, List.class);
            return response.getStatusCode().is2xxSuccessful() && !Objects.requireNonNull(response.getBody()).isEmpty();
        } catch (HttpClientErrorException.NotFound e) {
            return false; // Release does not exist
        }
    }

}
