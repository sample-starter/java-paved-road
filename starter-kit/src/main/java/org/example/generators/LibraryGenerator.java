package org.example.generators;

import org.example.dto.InitRequest;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.handlers.Handler;
import org.springframework.stereotype.Service;

@Service
public class LibraryGenerator {

    Handler libraryHandler;

    public void generate(final InitRequest initRequest) throws HandlerException {

        StarterRequest starterRequest = new StarterRequest(initRequest);
        libraryHandler.handle(starterRequest);

    }

}
