package br.com.brad.campaigningest.integration.fuction;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.dataMock.DataMock;
import br.com.brad.campaigningest.model.request.OptinMessage;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class IngestTest {

    private static final String ENDPOINT_PROCESS_MESSAGE = "/processMessage";
    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Tracer tracer;

    @Autowired
    private LocalValidatorFactoryBean validator;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Dado que envio body valido entao deve retornar estatus 200")
    @Test
    void processMessage() throws Exception {
        var body = DataMock.getOptinMessage();

        mockMvc.perform(post(ENDPOINT_PROCESS_MESSAGE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andDo(document("process-message-success",
                        requestFields(
                                fieldWithPath("email").description("Email do cliente para envio da proposta/convite").type(JsonFieldType.STRING),
                                fieldWithPath("name").description("Nome do cliente a ser usado no template do email").type(JsonFieldType.STRING),
                                fieldWithPath("dateSimulation").description("Data da simulação realizada pelo cliente (yyyy-MM-dd)").type(JsonFieldType.STRING),
                                fieldWithPath("amount").description("Valor simulado para o cliente").type(JsonFieldType.NUMBER)
                        )
                ));
    }

    @DisplayName("Dado que envio body invalido entao deve retornar estatus 400")
    @Test
    void processMessageError() throws Exception {
        var body = new OptinMessage(Strings.EMPTY, Strings.EMPTY, LocalDateTime.now(), BigDecimal.TEN);

        mockMvc.perform(post(ENDPOINT_PROCESS_MESSAGE)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}