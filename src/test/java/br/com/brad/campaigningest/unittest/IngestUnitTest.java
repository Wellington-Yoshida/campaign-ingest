package br.com.brad.campaigningest.unittest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.dataMock.DataMock;
import br.com.brad.campaigningest.exception.RequiredFieldException;
import br.com.brad.campaigningest.function.Ingest;
import br.com.brad.campaigningest.model.request.OptinMessage;

@ExtendWith(MockitoExtension.class)
public class IngestUnitTest {

    @InjectMocks
    private Ingest ingest;

    @Mock
    private Validator validator;

    @Mock
    private Tracer tracer;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Span span;

    @Mock
    private Tracer.SpanInScope spanInScope;

    private OptinMessage input;

    @BeforeEach
    void setUp() {
        input = DataMock.getOptinMessage();
        when(tracer.nextSpan()).thenReturn(span);
        when(span.name("processMessage")).thenReturn(span);
        when(span.start()).thenReturn(span);
        when(tracer.withSpan(span)).thenReturn(spanInScope);
    }

    @DisplayName("Dado que seja enviado body valido entao nao deve retornar erro")
    @Test
    void processMessageUnitTest() throws JsonProcessingException {
        when(validator.validate(input)).thenReturn(Collections.emptySet());
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"email\":\"cliente@dominio.com\",\"name\":\"Cliente Teste\",\"dateSimulation\":\"2025-09-02T20:45:23\",\"amount\":10}");

        assertDoesNotThrow(() -> ingest.processMessage().accept(input));
        verify(span).end();
    }

    @DisplayName("Dado que seja enviado body invalido entao deve retornar erro")
    @Test
    void processMessageUnitInvalidRequestWithOutNameAndEmailTest() {
        var input = DataMock.getOptinMessageWithOutNameAndEmail();

        @SuppressWarnings("unchecked")
        ConstraintViolation<OptinMessage> violation = Mockito.mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("campo obrigatório");
        when(validator.validate(input)).thenReturn(Set.of(violation));

        assertThrows(RequiredFieldException.class, () -> ingest.processMessage().accept(input));

        verify(span).end();
    }

}
