package org.example.handlers;

import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class ValidationHandler extends Handler {


    public ValidationHandler() {
    }

    public ValidationHandler(Handler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if (request.getGroupId() == null || request.getGroupId().isEmpty()) {
            throw new HandlerException("Group id cannot be null or empty");
        }

        if (request.getArtifactId() == null || request.getArtifactId().isEmpty()) {
            throw new HandlerException("Artifact id cannot be null or empty");
        }

        if (request.getVersion() == null || request.getVersion().isEmpty()) {
            throw new HandlerException("Version cannot be null or empty");
        }

        super.handle(request);
    }

}
