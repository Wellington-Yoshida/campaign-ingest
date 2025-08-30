package br.com.brad.campaigningest.model.message;

import java.math.BigDecimal;

import org.joda.time.DateTime;

public record OptinMessageSendRabbitMQ(String email, String name, DateTime dateSimulation, BigDecimal amount) {
}
