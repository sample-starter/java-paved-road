package org.example.exception;

import java.util.function.Function;

public class ExceptionWrapper {

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static <T, R> Function<T, R> wrapFunction(ThrowingFunction<T, R> throwingFunction) {
        return i -> {
            try {
                return throwingFunction.apply(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
