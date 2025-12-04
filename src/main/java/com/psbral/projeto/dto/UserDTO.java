package com.psbral.projeto.dto;

import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public record UserDTO(
        long id,

        @NotBlank(message = "Campo Obrigatório")
        @Size(min = 4, max = 50, message = "O nome deve ter entre 4 e 50 caracteres")
        String name,

        @NotBlank(message = "Campo Obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 254, message = "O email deve ter no máximo 254 caracteres")
        String email,

        @NotNull(message = "Campo Obrigatório")
        @PastOrPresent(message = "Data de Nascimento não pode ser futura")
        LocalDate birthDate,

        LocalDate createdAt,
        LocalDate lastUpdate

) { }
