package br.com.brad.campaigningest.model.message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OptinMessageSendRabbitMQ(String email, String name, LocalDateTime dateSimulation, BigDecimal amount) {
}
