package org.example.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.example.config.CommonConfig;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@AllArgsConstructor
@Component
public class GitHubUtil {

    private CommonConfig commonConfig;

    public void pushToGitHub(String orgName, String repoName) throws IOException, GitAPIException, URISyntaxException {

        log.info("Push changes to GitHub repository: {}", repoName);

        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), repoName);

        deleteLockFile(localPath);

        try (Git git = Git.open(localPath.toFile())) {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new org.eclipse.jgit.transport.URIish(String.format(commonConfig.getRepoPattern(), orgName, repoName)))
                    .call();

            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(commonConfig.getGitToken(), ""))
                    .call();
        }
    }

    private void deleteLockFile(Path localPath) throws IOException {
        Files.walk(localPath)
                .filter(path -> path.toString().endsWith(".lock"))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("Deleted lock file: " + path);
                    } catch (IOException e) {
                        log.info("Failed to delete lock file: " + path);
                    }
                });

    }

    public String getTemplatePath() {
        return System.getProperty("java.io.tmpdir") + this.commonConfig.getStarterId();

    }

    public String getTemplateId() {
        return this.commonConfig.getStarterId();

    }
}
