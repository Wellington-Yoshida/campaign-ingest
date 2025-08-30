package br.com.brad.campaigningest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import br.com.brad.campaigningest.model.message.OptinMessageSendRabbitMQ;
import br.com.brad.campaigningest.model.request.OptinMessage;

@Mapper
public interface OptinMessageMapper {

    OptinMessageMapper INSTANCE = Mappers.getMapper(OptinMessageMapper.class);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "dateSimulation", source = "dateSimulation")
    @Mapping(target = "amount", source = "amount")
    OptinMessageSendRabbitMQ convert(OptinMessage optinMessage);
}
