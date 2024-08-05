package org.example.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@Slf4j
public class InitRequest {

    private String userId;
    private String organization;
    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private RequestType requestType;
    private List<DependencyRequest> dependencies;

}
