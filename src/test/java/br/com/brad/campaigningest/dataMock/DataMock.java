package br.com.brad.campaigningest.dataMock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.experimental.UtilityClass;

import br.com.brad.campaigningest.model.request.OptinMessage;

@UtilityClass
public class DataMock {

    public OptinMessage getOptinMessage() {
        return new OptinMessage("cliente@dominio.com", "Cliente Teste", LocalDateTime.now(), BigDecimal.TEN);
    }

    public OptinMessage getOptinMessageWithOutNameAndEmail() {
        return new OptinMessage("", "", LocalDateTime.now(), BigDecimal.TEN);
    }
}
