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

/**
 * Configuração responsável pelo fluxo de ingestão de mensagens de opt-in dos clientes que não finalizaram a contratacao de CP.
 * Mensagens sera usada para enviar proposta de continuar a contratacao.
 *
 * <p>Exposta como bean funcional ({@code Consumer<OptinMessage>}), esta classe:
 * <ol>
 *   <li>Inicia um <em>span</em> de rastreamento para o processamento;</li>
 *   <li>Valida a entrada usando {@link Validator};</li>
 *   <li>Converte o DTO de requisição para o formato de saída via {@link OptinMessageMapper};</li>
 *   <li>Serializa a mensagem para JSON com {@link ObjectMapper};</li>
 *   <li>Publica no RabbitMQ através de {@link RabbitTemplate}.</li>
 * </ol>
 *
 * <h2>Observações</h2>
 * <ul>
 *   <li>Logs de depuração são emitidos para início e fim do processamento.</li>
 *   <li>O <em>span</em> criado é finalizado ao término do fluxo, com ou sem erro.</li>
 * </ul>
 *
 * @since 1.0
 */
@Log4j2
@Configuration
@RequiredArgsConstructor
public class Ingest {

    private final Validator validated;
    /**
     * Componente de rastreamento (Micrometer) para criação e escopo do <em>span</em>.
     */
    private final Tracer tracer;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Cria o consumidor funcional responsável por processar mensagens de opt-in.
     *
     * <p>Passos executados:</p>
     * <ol>
     *   <li>Abre um novo <em>span</em> chamado {@code processMessage};</li>
     *   <li>Valida a requisição e lança {@link RequiredFieldException} em caso de erros;</li>
     *   <li>Converte a entrada com {@link OptinMessageMapper#INSTANCE};</li>
     *   <li>Serializa para JSON e envia para o RabbitMQ (exchange {@code "campaign"},
     *       routing key {@code "campaign"});</li>
     *   <li>Finaliza o <em>span</em> no bloco {@code finally}.</li>
     * </ol>
     *
     * @return um {@link Consumer} que processa instâncias de {@link OptinMessage}
     * @throws RequiredFieldException em tempo de execução, quando a validação falhar
     * @throws RuntimeException em tempo de execução, encapsulando {@link JsonProcessingException}
     */
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
