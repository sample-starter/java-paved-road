package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.example.config.TemplateConfig;
import org.example.dto.DependencyRequest;
import org.example.dto.RequestType;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.utils.*;
import org.jdom2.Document;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class CodeCreationHandler extends Handler {

    private final GitHubUtil gitHubUtil;
    private final TemplateConfig templateConfig;

    public CodeCreationHandler(GitHubUtil gitHubUtil, TemplateConfig templateConfig) {
        this.gitHubUtil = gitHubUtil;
        this.templateConfig = templateConfig;
    }

    public CodeCreationHandler(Handler nextHandler, GitHubUtil gitHubUtil, TemplateConfig templateConfig) {
        super(nextHandler);
        this.gitHubUtil = gitHubUtil;
        this.templateConfig = templateConfig;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        request.getDependencies().stream()
                .sorted(Comparator.comparingInt(DependencyRequest::getPriority))
                .map(this::getStarterConfig)
                .forEach(config -> processStarterConfig(config, request));

        super.handle(request);
    }


    private TemplateConfig.StarterConfig getStarterConfig(DependencyRequest request) {
        String templateId = request.getTemplateId();
        return templateConfig.getStarterConfig(templateId);
    }

    private void processStarterConfig(TemplateConfig.StarterConfig config, StarterRequest request) throws RuntimeException {
        try {
            Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), request.getArtifactId());
            addCodeSample(localPath, config, request);

            if(request.getRequestType() == RequestType.SERVICE) {
                // Initialize Git repository
                Git.init().setDirectory(localPath.toFile()).call();

                // Add files to Git
                try (Git git = Git.open(localPath.toFile())) {
                    git.add().addFilepattern(".").call();
                    git.commit().setMessage(String.format("Committing %s Sample changes", config.getStarter())).call();
                }
                gitHubUtil.pushToGitHub(request.getArtifactId());
            }
        } catch (IOException | GitAPIException | URISyntaxException | JDOMException e) {
            request.setStatus(StarterRequest.Status.FAILURE);
            throw new RuntimeException(e);
        }
    }

    private void addCodeSample(Path localPath, TemplateConfig.StarterConfig starterConfig, StarterRequest request) throws IOException, JDOMException, GitAPIException {

        Files.createDirectories(localPath.resolve(request.getTargetCodePath()));

        for(String path : starterConfig.getPaths()) {
            log.info("Copying source code from {} for repo :{}" , path, request.getArtifactId());
            StarterUtil.copySourceModuleToLocalPath(path,
                    localPath.resolve(request.getTargetCodePath()+"/"+starterConfig.getTarget()));
        }

        //merge application.yaml file
        log.info("Update application.yml for {} for repo :{}" , starterConfig.getStarter(), request.getArtifactId());
        String existingYamlPath = request.getTargetResourcePath()+"/application.yml";
        Map<String, Object> yamlData1 = YamlMerger.loadYaml(localPath.resolve(existingYamlPath).toString());
        Map<String, Object> yamlData2 = YamlMerger.loadYaml(starterConfig.getApplication());
        Map<String, Object> mergedYamlData = YamlMerger.mergeYamlData(yamlData1, yamlData2);
        YamlMerger.saveYaml(mergedYamlData, localPath.resolve(existingYamlPath));


        //merge pom.xml file
        log.info("Update pom.xml for {} for repo :{}" ,starterConfig.getStarter(), request.getArtifactId());
        Document pomDoc = PomUtil.mergePoms(localPath.resolve(request.getTargetPomPath()).toString(), starterConfig.getPom());
        PomUtil.saveDocument(pomDoc, localPath.resolve(request.getTargetPomPath()));

        createManifest(localPath);
    }

    public void createManifest(Path localPath) throws IOException {
        List<String> fileNames;
        try (Stream<Path> paths = Files.walk(localPath)) {
            fileNames = paths
                    .filter(Files::isRegularFile)
                    .map(localPath::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }

        Files.write(localPath.resolve("manifest.txt"), fileNames);
    }

}
