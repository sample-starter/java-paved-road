package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.example.config.CommonConfig;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.jdom2.JDOMException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class RepositoryHandler extends Handler {

    private CommonConfig commonConfig;

    public RepositoryHandler(CommonConfig config) {
        this.commonConfig = config;
    }

    public RepositoryHandler(Handler nextHandler, CommonConfig config) {
        super(nextHandler);
        this.commonConfig = config;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {
        getRepository(request);
        super.handle(request);
    }

    public void getRepository(StarterRequest request) throws HandlerException {

        try {
            if (repositoryExists(request.getArtifactId())) {
                log.info("Repository exists, opening...");

            } else {
                log.info("Repository does not exist, creating...");
                createGitHubRepository(request.getOrganization(), request.getArtifactId(), request.getDescription(), false);
            }
            openTemplateRepository();
            openRepository(request.getOrganization(), request.getArtifactId());
            getStaterStatus(request);
        }
        catch (IOException | GitAPIException | JDOMException e) {
            request.setStatus(StarterRequest.Status.FAILURE);
            throw new HandlerException(e);
        }
    }

    public boolean repositoryExists(String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + commonConfig.getGitToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://github.com/sample-starter/" + repoName + ".git",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public void createGitHubRepository(String orgName, String repoName, String description, boolean isPrivate) {
        RestTemplate restTemplate = new RestTemplate();

        JSONObject json = new JSONObject();
        json.put("name", repoName);
        json.put("description", description);
        json.put("private", isPrivate);
        json.put("default_branch", "main");
        json.put("auto_init", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + commonConfig.getGitToken());

        HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                commonConfig.getGitApiUrl() + String.format("/orgs/%s/repos", orgName),
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("Repository created successfully: " + response.getBody());
        } else {
            log.error("Failed to create repository: " + response.getBody());
        }
    }

    public void openTemplateRepository() throws IOException, GitAPIException {
        String repoUrl = commonConfig.getStarterPattern();
        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), commonConfig.getStarterId());

        // Clone the repository if it's not already cloned locally
        if (!Files.exists(localPath)) {
            Git result = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localPath.toFile())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(commonConfig.getGitToken(), ""))
                    .call();

            result.getRepository().close();
            result.close();
        }
    }

    public void openRepository(String orgName, String repoName) throws IOException, GitAPIException {
//        String repoUrl = "https://github.com/sample-starter/"  + repoName + ".git";
        String repoUrl = String.format(commonConfig.getRepoPattern(), orgName, repoName);
        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), repoName);

        // Clone the repository if it's not already cloned locally
        if (!Files.exists(localPath)) {
            Git result = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localPath.toFile())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(commonConfig.getGitToken(), ""))
                    .call();

            result.getRepository().close();
            result.close();
        }
    }

    public static void getStaterStatus(StarterRequest starterRequest) throws IOException, JDOMException {
        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), starterRequest.getArtifactId());
//        localPath.resolve("starter.xml")
//        File starterXML = localPath.resolve("starter.xml").toFile();
//        Document document = new SAXBuilder().build(starterXML);
//        starterRequest.setCheckPoint(document);
    }

}
