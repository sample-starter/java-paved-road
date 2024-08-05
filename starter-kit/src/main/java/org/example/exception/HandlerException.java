package org.example.exception;

import org.example.handlers.Handler;

public class HandlerException extends Exception {

    public HandlerException(Exception e, String message) {
        super(message, e);
    }

    public HandlerException(Exception e) {
        super(e);
    }

    public HandlerException(String message) {
        super(message);
    }

}
