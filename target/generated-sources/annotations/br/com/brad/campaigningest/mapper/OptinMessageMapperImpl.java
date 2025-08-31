package br.com.brad.campaigningest.mapper;

import br.com.brad.campaigningest.model.message.OptinMessageSendRabbitMQ;
import br.com.brad.campaigningest.model.request.OptinMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-31T17:44:16-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
public class OptinMessageMapperImpl implements OptinMessageMapper {

    @Override
    public OptinMessageSendRabbitMQ convert(OptinMessage optinMessage) {
        if ( optinMessage == null ) {
            return null;
        }

        String name = null;
        LocalDateTime dateSimulation = null;
        BigDecimal amount = null;
        String email = null;

        name = optinMessage.name();
        dateSimulation = optinMessage.dateSimulation();
        amount = optinMessage.amount();
        email = optinMessage.email();

        OptinMessageSendRabbitMQ optinMessageSendRabbitMQ = new OptinMessageSendRabbitMQ( email, name, dateSimulation, amount );

        return optinMessageSendRabbitMQ;
    }
}
