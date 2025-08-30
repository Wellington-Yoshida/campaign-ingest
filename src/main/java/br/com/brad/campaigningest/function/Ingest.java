package br.com.brad.campaigningest.function;

import java.util.Objects;
import java.util.function.Function;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;

import br.com.brad.campaigningest.exception.RequiredFieldException;
import br.com.brad.campaigningest.mapper.OptinMessageMapper;
import br.com.brad.campaigningest.model.request.OptinMessage;

@Configuration
@AllArgsConstructor
public class Ingest {

    private final Validator validated;

    @Bean
    public Function<OptinMessage, String> processMessage() {
        return value -> {
            var result = validated.validate(value);
            if (Objects.nonNull(result) && !result.isEmpty()) {
                final var listErrors = result.stream().map(ConstraintViolation::getMessage).toList();
                throw new RequiredFieldException(listErrors);
            }
            // TODO realizar envio da mensagem para o SQS
            return OptinMessageMapper.INSTANCE.convert(value).toString();
        };
    }

}
