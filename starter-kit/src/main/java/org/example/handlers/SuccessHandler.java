package org.example.handlers;

import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;

public class SuccessHandler extends Handler {

    public SuccessHandler() {
    }

    public SuccessHandler(Handler nextHandler) {
        super(nextHandler);
    }

    @Override
    public void handle(final StarterRequest request) throws HandlerException {

        if(request.getStatus() == StarterRequest.Status.SUCCESS) {

            //Update SQL DB with repository link and success status.

        }

        super.handle(request);
    }

}
