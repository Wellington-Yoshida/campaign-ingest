package br.com.brad.campaigningest.integration.fuction;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.tracing.Tracer;

import br.com.brad.campaigningest.model.request.OptinMessage;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ExtendWith(SpringExtension.class)
public class IngestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Tracer tracer;

    @MockitoBean
    private LocalValidatorFactoryBean validator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processMessage() throws Exception {
        var body = new OptinMessage("cliente@dominio.com", "Cliente Teste", LocalDateTime.now(), BigDecimal.TEN);

        Mockito.when(validator.validate(Mockito.any())).thenReturn(java.util.Collections.emptySet());

        mockMvc.perform(post("/processMessage")
                        .contentType("application/json")
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
}