package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class GitHubUtil {

    private static final String TOKEN = "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";
    private static final String ORG_URI = "https://github.com/sample-starter/";

    public static void pushToGitHub(String repoName) throws IOException, GitAPIException, URISyntaxException {

        log.info("Push changes to GitHub repository: {}", repoName);

        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), repoName);

        try (Git git = Git.open(localPath.toFile())) {
            git.remoteAdd()
                    .setName("origin")
                    .setUri(new org.eclipse.jgit.transport.URIish(ORG_URI+repoName+".git"))
                    .call();

            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(TOKEN, ""))
                    .call();
        }
    }

}
