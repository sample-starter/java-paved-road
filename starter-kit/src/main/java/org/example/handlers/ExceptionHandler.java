package org.example.handlers;

import org.example.config.CommonConfig;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class ExceptionHandler extends Handler {

    private static final String GITHUB_API_URL = "https://api.github.com";
    private CommonConfig commonConfig;


    public ExceptionHandler(CommonConfig config) {
        this.commonConfig = config;
    }

    public ExceptionHandler(Handler nextHandler, CommonConfig config) {
        super(nextHandler);
        this.commonConfig = config;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if(request.getStatus() == StarterRequest.Status.FAILURE) {

            //Update SQL DB with Exception status.
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + commonConfig.getGitToken());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        GITHUB_API_URL + "/orgs/sample-starter/repos",
                        HttpMethod.POST,
                        entity,
                        String.class,
                        "shik1409",
                        request.getArtifactId()
                );

            } catch (HttpClientErrorException e) {
                new HandlerException(e);
            }

            //update SQL DB with Exception status

        }

        super.handle(request);
    }

}
