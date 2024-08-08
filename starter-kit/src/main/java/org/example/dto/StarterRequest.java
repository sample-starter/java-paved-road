package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Getter
@Setter
public class StarterRequest {

    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private List<DependencyRequest> dependencies;
    private String targetCodePath;
    private String targetResourcePath;
    private String targetPomPath;
    private Document starterCheckPoint;
    private RequestType requestType;
    private Status status;
    private String repoName;
    private String organization;

    public StarterRequest(InitRequest request) {

        this.artifactId = request.getArtifactId();
        this.groupId = request.getGroupId();
        this.version = request.getVersion();
        this.description = request.getDescription();
        this.dependencies = request.getDependencies();
        this.organization = request.getOrganization();

        if (this.groupId == null) {
            // Handle the null case, e.g., set a default value or throw an exception
            this.groupId = "default/group/id";  // Replace with your default value or handling logic
        }

        this.targetCodePath = new StringJoiner("/")
                .add("src/main/java")
                .add(this.groupId.replace('.','/')).toString();

        this.targetResourcePath = "src/main/resources";
        this.targetPomPath = "pom.xml";
        this.status = Status.SUCCESS;
        this.requestType = request.getRequestType();
        this.repoName = String.format("https://github.com/%s/%s.git", this.organization, this.artifactId);
    }

//    public void setCheckPoint(Document starterCheckPoint) {
//        this.starterCheckPoint = starterCheckPoint;
//
//        Element rootElement = this.starterCheckPoint.getRootElement();
//        if(Objects.nonNull(rootElement)) {
//            this.dependencies.forEach(dep -> {
//                if(Objects.nonNull(rootElement.getChild(dep.toString()))) {
//                    dep.setDone(true);
//                }
//            });
//        }
//    }

    public static enum Status{
        SUCCESS,
        FAILURE;
    }

}
