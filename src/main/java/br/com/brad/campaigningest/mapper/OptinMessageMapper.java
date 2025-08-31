package br.com.brad.campaigningest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import br.com.brad.campaigningest.model.message.OptinMessageSendRabbitMQ;
import br.com.brad.campaigningest.model.request.OptinMessage;

/**
 * Mapeador responsável por converter a requisição de opt-in ({@link OptinMessage})
 * para a mensagem de saída que será publicada no RabbitMQ
 * ({@link OptinMessageSendRabbitMQ}).
 *
 * <p>Este mapper utiliza MapStruct para geração de código em tempo de compilação.
 * Campos com o mesmo nome e tipo são mapeados diretamente; mapeamentos explícitos
 * abaixo documentam a intenção e facilitam evoluções futuras.</p>
 *
 * <h2>Políticas</h2>
 * <ul>
 *   <li>Null handling: campos nulos são propagados como nulos (sem defaults).</li>
 *   <li>Conversões: não há conversões customizadas de tipos neste mapper.</li>
 * </ul>
 *
 * @see OptinMessage
 * @see OptinMessageSendRabbitMQ
 * @since 1.0.0
 */
@Mapper
public interface OptinMessageMapper {

    /**
     * Instância singleton gerenciada pelo MapStruct.
     */
    OptinMessageMapper INSTANCE = Mappers.getMapper(OptinMessageMapper.class);


    /**
     * Converte a requisição de opt-in em uma mensagem pronta para publicação no RabbitMQ.
     *
     * @param optinMessage objeto de entrada com dados do opt-in; pode ser nulo
     * @return objeto {@code OptinMessageSendRabbitMQ} mapeada para publicação; retorna nulo se {@code optinMessage} for nulo
     */
    @Mapping(target = "name", source = "name")
    @Mapping(target = "dateSimulation", source = "dateSimulation")
    @Mapping(target = "amount", source = "amount")
    OptinMessageSendRabbitMQ convert(OptinMessage optinMessage);
}
