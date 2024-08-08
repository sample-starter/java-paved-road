package org.example.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(type = "string", example = "sample-starter")
    private String organization;
    @Schema(type = "string", example = "org.example")
    private String groupId;
    @Schema(type = "string", example = "sample")
    private String artifactId;
    @Schema(type = "string", example = "1.0-SNAPSHOT")
    private String version;
    private String description;
    @Schema(type = "string", example = "SERVICE")
    private RequestType requestType;
    @ArraySchema(arraySchema = @Schema(implementation = DependencyRequest.class, example ="[\"API\", \"KAFKA_CONSUMER\" , \"SQL\"]"))
    private List<DependencyRequest> dependencies;

}
