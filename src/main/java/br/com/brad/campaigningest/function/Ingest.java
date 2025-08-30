package br.com.brad.campaigningest.function;

import java.util.Objects;
import java.util.function.Consumer;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.exception.RequiredFieldException;
import br.com.brad.campaigningest.mapper.OptinMessageMapper;
import br.com.brad.campaigningest.model.request.OptinMessage;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class Ingest {

    private final Validator validated;
    private final Tracer tracer;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<OptinMessage> processMessage() {
        return value -> {
            var newSpan = tracer.nextSpan().name("processMessage").start();

            try(Tracer.SpanInScope ws = tracer.withSpan(newSpan)) {
                log.debug("Iniciando processamento da mensagem de opt-in");

                validateOrThrow(value);
                var optinMessage = OptinMessageMapper.INSTANCE.convert(value);
                rabbitTemplate.convertAndSend("campaign", "campaign",
                        objectMapper.writeValueAsString(optinMessage));

                log.debug("Processamento concluído com sucesso");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } finally {
                newSpan.end();
            }
        };
    }

    private void validateOrThrow(OptinMessage value) {
        var result = validated.validate(value);
        if (Objects.nonNull(result) && !result.isEmpty()) {
            final var listErrors = result.stream().map(ConstraintViolation::getMessage).toList();
            throw new RequiredFieldException(listErrors);
        }
    }
}
