package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.InitRequest;
import org.example.dto.RequestType;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.generators.LibraryGenerator;
import org.example.generators.ServiceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/starter-api")
@Tag(name = "Starter API")
public class InitController {

    ServiceGenerator serviceGenerator;
    LibraryGenerator libraryGenerator;

    @Autowired
    public InitController(ServiceGenerator serviceGenerator, LibraryGenerator libraryGenerator) {
        this.serviceGenerator = serviceGenerator;
        this.libraryGenerator = libraryGenerator;
    }


    @PostMapping("/createGitHubRepository")
    public String createWebService(@RequestBody InitRequest initRequest) {
        if(initRequest.getRequestType() == RequestType.SERVICE) {
            try {
                StarterRequest request = serviceGenerator.generate(initRequest);
                return request.getRepoName();
            } catch (HandlerException e) {
                throw new RuntimeException(e);
            }
        }

        return "Failure";

    }

    @PostMapping("/createLibrary")
    public String createLibrary(@RequestBody InitRequest initRequest) {
        if(initRequest.getRequestType() == RequestType.LIBRARY) {
            try {
                libraryGenerator.generate(initRequest);
            } catch (HandlerException e) {
                throw new RuntimeException(e);
            }
        }
        return "result";
    }

}
