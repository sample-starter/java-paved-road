package org.example.generators;

import lombok.AllArgsConstructor;
import org.example.dto.InitRequest;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.handlers.Handler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ServiceGenerator {

    Handler validationHandler;
    Handler exceptionHandler;

    public StarterRequest generate(final InitRequest initRequest) throws HandlerException {

        StarterRequest starterRequest = new StarterRequest(initRequest);
        try {
            validationHandler.handle(starterRequest);
        }
        catch (HandlerException e) {
            exceptionHandler.handle(starterRequest);
        }

        return starterRequest;

    }

}
