package org.example.generators;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.dto.InitRequest;
import org.example.dto.StarterRequest;
import org.example.exception.HandlerException;
import org.example.handlers.Handler;
import org.example.handlers.ValidationHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@FieldDefaults(level = AccessLevel.PRIVATE)
class ServiceGeneratorTest {

    @Mock
    Handler handler2;

    @Mock
    Handler handler3;

    ServiceGenerator generator;

    @BeforeAll
    void setup() {
        Handler handler1 = new ValidationHandler(handler2);
        generator = new ServiceGenerator(handler1, handler3);

    }

    @Test
    void testExceptionHandler() throws HandlerException {
        generator.generate(Mockito.mock(InitRequest.class));
        verify(handler3).handle(Mockito.any(StarterRequest.class));
    }

    @Test
    void testSuccessHandler() throws HandlerException {

        InitRequest initRequest = new InitRequest();
        initRequest.setArtifactId("someArtifact");
        initRequest.setGroupId("groupid");
        initRequest.setVersion("1.0");

        generator.generate(initRequest);
        verify(handler2).handle(Mockito.any(StarterRequest.class));
    }


}