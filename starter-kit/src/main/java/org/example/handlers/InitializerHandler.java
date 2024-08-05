package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.example.dto.RequestType;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.utils.GitHubUtil;
import org.example.utils.PomUtil;
import org.example.utils.StarterUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InitializerHandler extends Handler {

    private final GitHubUtil gitHubUtil;

    public InitializerHandler(GitHubUtil gitHubUtil) {
        super();
        this.gitHubUtil = gitHubUtil;
    }

    public InitializerHandler(Handler nextHandler, GitHubUtil gitHubUtil) {
        super(nextHandler);
        this.gitHubUtil = gitHubUtil;
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {
        try {
            initializeLocalRepository(request);
            if(request.getRequestType() == RequestType.SERVICE) {
                gitHubUtil.pushToGitHub(request.getArtifactId());
            }
        }
        catch (IOException | GitAPIException | URISyntaxException | JDOMException e) {
            throw new HandlerException(e);
        }

        super.handle(request);
    }

    public void initializeLocalRepository(final StarterRequest request) throws IOException, GitAPIException, JDOMException {
        Path localPath = Paths.get(System.getProperty("java.io.tmpdir"), request.getArtifactId());
        Files.createDirectories(localPath);

        log.info("Copying starter-common code");

        Files.createDirectories(localPath.resolve(request.getTargetCodePath()));
        StarterUtil.copySourceModuleToLocalPath("starter-common/src/main/java/org/example/common",
                localPath.resolve(request.getTargetCodePath()));

        log.info("Copying starter-common resources");
        StarterUtil.copySourceModuleToLocalPath("starter-common/src/main/resources",
                localPath.resolve(request.getTargetResourcePath()));


        //initialize root files
        log.info("Copying starter-common root resources");
        StarterUtil.copySourceModuleToLocalPath("starter-common/src/main/root-resources", localPath);

        //pom.xml initialization
        log.info("Modifying pom.xml file");
        PomUtil.saveDocument(initializePOM(request, localPath.resolve(request.getTargetPomPath())), localPath.resolve(request.getTargetPomPath()));

        adjustReadme(localPath.resolve("README.md"), request.getArtifactId());

        // Create manifest file
        createManifest(localPath);

        // Initialize Git repository
        Git.init().setDirectory(localPath.toFile()).call();

        // Add files to Git
        log.info("Create Initial Commit");
        try (Git git = Git.open(localPath.toFile())) {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        }
    }

    private void adjustReadme(Path resolve, String artifactId) throws IOException {

        List<String> lines = Files.readAllLines(resolve, StandardCharsets.UTF_8);

        if (!lines.isEmpty()) {
            // Replace the first line if it starts with a heading indicator ('#')
            if (lines.get(0).startsWith("#")) {
                lines.set(0, "# " + artifactId);
            } else {
                // Otherwise, insert the new heading at the beginning
                lines.add(0, "# " + artifactId);
            }
        } else {
            // If the file is empty, just add the new heading
            lines.add("# " + artifactId);
        }

        Files.write(resolve, lines, StandardCharsets.UTF_8);

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

    public Document initializePOM(final StarterRequest request, Path rootPom) throws IOException, JDOMException {

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(rootPom.toFile());

        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        rootElement.getChild("groupId", namespace).setText(request.getGroupId());
        rootElement.getChild("artifactId", namespace).setText(request.getArtifactId());
        rootElement.getChild("version", namespace).setText(request.getVersion());
        rootElement.getChild("name", namespace).setText(request.getArtifactId());
        rootElement.getChild("description", namespace).setText(request.getDescription());

        return document;

    }

}
