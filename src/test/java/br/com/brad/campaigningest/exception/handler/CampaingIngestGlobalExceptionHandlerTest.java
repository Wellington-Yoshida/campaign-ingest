package br.com.brad.campaigningest.exception.handler;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.exception.RequiredFieldException;

@ExtendWith(MockitoExtension.class)
class CampaingIngestGlobalExceptionHandlerTest {

    private static final String TRACEID = "123456789";
    private static final String PATH = "/campaign/ingest";
    private static final String MESSAGE_ERRO = "Erro de validação no corpo da requisição.";
    private static final String PROPERTIES_TRACEID = "traceId";

    @InjectMocks
    private CampaingIngestGlobalExceptionHandler exceptionHandler;

    @Mock
    private Tracer tracer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    @DisplayName("Dado que seja chamado exception RequiredFieldException entao deve retorar um ProblemDetail")
    @Test
    void requiredFieldExceptionTest() {
        Mockito.when(request.getRequestURI()).thenReturn(PATH);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(traceContext);
        Mockito.when(traceContext.traceId()).thenReturn(TRACEID);

        final var result = exceptionHandler.requiredFieldException(new RequiredFieldException(List.of(Strings.EMPTY)), request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        Assertions.assertEquals(MESSAGE_ERRO, result.getDetail());
        Assertions.assertNotNull(result.getProperties());
        Assertions.assertEquals(TRACEID, result.getProperties().get(PROPERTIES_TRACEID));
    }

    @DisplayName("Dado que seja chamado exception RequiredFieldException e nao tenha traceID entao deve retorar um ProblemDetail com traceID gerado em tempo real")
    @Test
    void requiredFieldExceptionWithoutTraIDTest() {
        Mockito.when(request.getRequestURI()).thenReturn(PATH);
        Mockito.when(tracer.currentSpan()).thenReturn(null);

        final var result = exceptionHandler.requiredFieldException(new RequiredFieldException(List.of(Strings.EMPTY)), request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        Assertions.assertEquals(MESSAGE_ERRO, result.getDetail());
        Assertions.assertNotNull(result.getProperties());
        Assertions.assertNotNull(result.getProperties().get(PROPERTIES_TRACEID));
    }
}
