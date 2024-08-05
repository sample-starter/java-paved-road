package org.example.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;

@Slf4j
public abstract class Handler {

    private Handler nextHandler;

    public Handler() {
        this.nextHandler = null;
    }
    public Handler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public Handler setNext(Handler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public void handle(final StarterRequest request) throws HandlerException {
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }

}
