package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.utils.GitHubUtil;
import org.example.utils.StarterUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CICDHandler extends Handler {

    private GitHubUtil gitHubUtil;

    public CICDHandler(GitHubUtil gitHubUtil) {
        this.gitHubUtil = gitHubUtil;
    }

    public CICDHandler(Handler nextHandler, GitHubUtil gitHubUtil) {
        super(nextHandler);
        this.gitHubUtil = gitHubUtil;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if(request.getStatus() == StarterRequest.Status.SUCCESS) {
            try {
                addCICDSample(request);
                gitHubUtil.pushToGitHub(request.getArtifactId());
            }
            catch(IOException | GitAPIException | URISyntaxException e) {
                throw new HandlerException(e);
            }
        }

        super.handle(request);
    }

    private void addCICDSample(StarterRequest request) throws IOException, GitAPIException {

        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), request.getArtifactId());
        Files.createDirectories(localPath.resolve(request.getTargetCodePath()));

        log.info("Copying source code from start-cicd for repo :{}" , request.getArtifactId());
        StarterUtil.copySourceModuleToLocalPath("starter-cicd/src/main/resources", localPath);

        adjustPipeline(localPath.resolve(".github/workflows"), request.getArtifactId());
        adjustPipeline(localPath.resolve("k8s"), request.getArtifactId());

        // Initialize Git repository
        Git.init().setDirectory(localPath.toFile()).call();

        // Add files to Git
        try (Git git = Git.open(localPath.toFile())) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Committing CI-CD Sample workflows").call();
        }

    }

    private void adjustPipeline(Path targetPath, String artifactId) throws IOException {

        // Get a list of all YAML files in the directory
        List<Path> yamlFiles = Files.walk(targetPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".yaml") || path.toString().endsWith(".yml"))
                .toList();

        // Process each YAML file
        for (Path filePath : yamlFiles) {
            replaceTextInYamlFile(filePath, "sample-spring-boot-app", artifactId);
        }

    }

    public void replaceTextInYamlFile(Path filePath, String searchString, String replacementString) throws IOException {
        // Read the content of the YAML file
        String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);

        // Replace the specified string
        content = content.replace(searchString, replacementString);

        // Write the updated content back to the file
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
    }


}
