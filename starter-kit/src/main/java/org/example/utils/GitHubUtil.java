package org.example.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.example.config.CommonConfig;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@AllArgsConstructor
@Component
public class GitHubUtil {

    private CommonConfig commonConfig;
    private static final String ORG_URI = "https://github.com/sample-starter/";

    public void pushToGitHub(String repoName) throws IOException, GitAPIException, URISyntaxException {

        log.info("Push changes to GitHub repository: {}", repoName);

        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), repoName);

        try (Git git = Git.open(localPath.toFile())) {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new org.eclipse.jgit.transport.URIish(ORG_URI+repoName+".git"))
                    .call();

            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(commonConfig.getGitToken(), ""))
                    .call();
        }
    }

}
