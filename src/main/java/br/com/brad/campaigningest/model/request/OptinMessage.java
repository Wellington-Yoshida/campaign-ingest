package br.com.brad.campaigningest.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record OptinMessage(@NotBlank(message = "Campo email não pode ser nulo/vazio.")
                           @Email(message = "Email no formato incorreto.")
                           String email,
                           @NotBlank(message = "Campo name não pode ser nulo/vazio")
                           String name,
                           @NotNull(message = "Campo dateSimulation não pode ser nulo")
                           LocalDateTime dateSimulation,
                           @NotNull(message = "Campo amount não pode ser nulo")
                           BigDecimal amount) {
}
