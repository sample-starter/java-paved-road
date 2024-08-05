package org.example.handlers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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

public class RepositoryHandler extends Handler {

    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String TOKEN = "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";
    private static final String ORG_URI = "https://github.com/sample-starter/";

    public RepositoryHandler() {
    }

    public RepositoryHandler(Handler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {
        getRepository(request);
        super.handle(request);
    }

    public static void getRepository(StarterRequest request) throws HandlerException {

        try {
            if (repositoryExists(request.getArtifactId())) {
                System.out.println("Repository exists, opening...");

            } else {
                System.out.println("Repository does not exist, creating...");
                createGitHubRepository(request.getArtifactId(), request.getDescription(), false);
            }
            openRepository(request.getArtifactId());
            getStaterStatus(request);
        }
        catch (IOException | GitAPIException | JDOMException e) {
            throw new HandlerException(e);
        }
    }

    public static boolean repositoryExists(String repoName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + TOKEN);
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

    public static void createGitHubRepository(String repoName, String description, boolean isPrivate) {
        RestTemplate restTemplate = new RestTemplate();

        JSONObject json = new JSONObject();
        json.put("name", repoName);
        json.put("description", description);
        json.put("private", isPrivate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "token " + TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GITHUB_API_URL + "/orgs/sample-starter/repos",
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Repository created successfully: " + response.getBody());
        } else {
            System.out.println("Failed to create repository: " + response.getBody());
        }
    }

    public static void openRepository(String repoName) throws IOException, GitAPIException {
        String repoUrl = "https://github.com/sample-starter/"  + repoName + ".git";
        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), repoName);

        // Clone the repository if it's not already cloned locally
        if (!Files.exists(localPath)) {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localPath.toFile())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(TOKEN, ""))
                    .call();
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
