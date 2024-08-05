package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.example.dto.DependencyRequest;
import org.example.dto.RequestType;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.utils.GitHubUtil;
import org.example.utils.PomUtil;
import org.example.utils.StarterUtil;
import org.example.utils.YamlMerger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class ApiHandler extends Handler {

    private static final String TARGET_FOLDER = "/api";

    private GitHubUtil gitHubUtil;

    public ApiHandler(GitHubUtil gitHubUtil) {
        this.gitHubUtil = gitHubUtil;
    }

    public ApiHandler(Handler nextHandler, GitHubUtil gitHubUtil) {
        super(nextHandler);
        this.gitHubUtil = gitHubUtil;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if(request.getDependencies().contains(DependencyRequest.API)) {

            try {
                addAPISample(request);
                if(request.getRequestType() == RequestType.SERVICE) {
                    gitHubUtil.pushToGitHub(request.getArtifactId());
                }
            } catch (IOException | GitAPIException | URISyntaxException | JDOMException e) {
                throw new HandlerException(e);
            }
        }

        super.handle(request);
    }

    private void addAPISample(StarterRequest request) throws IOException, JDOMException, GitAPIException {

        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), request.getArtifactId());
        Files.createDirectories(localPath.resolve(request.getTargetCodePath()));

        log.info("Copying source code from start-api for repo :{}" , request.getArtifactId());
        StarterUtil.copySourceModuleToLocalPath("starter-api/src/main/java/org/example/api",
                localPath.resolve(request.getTargetCodePath()+TARGET_FOLDER));

        //merge application.yaml file
        log.info("Update application.yml for start-api for repo :{}" , request.getArtifactId());
        String existingYamlPath = request.getTargetResourcePath()+"/application.yml";
        Map<String, Object> yamlData1 = YamlMerger.loadYaml(localPath.resolve(existingYamlPath).toString());
        Map<String, Object> yamlData2 = YamlMerger.loadYaml("starter-api/src/main/resources/application.yml");
        Map<String, Object> mergedYamlData = YamlMerger.mergeYamlData(yamlData1, yamlData2);
        YamlMerger.saveYaml(mergedYamlData, localPath.resolve(existingYamlPath));


        //merge pom.xml file
        log.info("Update pom.xml for start-api for repo :{}" , request.getArtifactId());
        Document pomDoc = PomUtil.mergePoms(localPath.resolve(request.getTargetPomPath()).toString(), "starter-api/pom.xml");
        PomUtil.saveDocument(pomDoc, localPath.resolve(request.getTargetPomPath()));

        // Initialize Git repository
        Git.init().setDirectory(localPath.toFile()).call();

        // Add files to Git
        try (Git git = Git.open(localPath.toFile())) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Committing API Sample changes").call();
        }

    }


}
