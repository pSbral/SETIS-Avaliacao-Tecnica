package com.psbral.projeto.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {

    // NEEDLESS?
    private long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 4, max = 50, message = "O nome deve ter entre 4 e 50 caracteres")
    private String nome;

    @Email(message = "Formato de e-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    @Size(max = 254, message = "O email deve ter no máximo 254 caracteres")
    private String email;

    @NotNull(message = "Data de nascimento é obrigatória")
    @PastOrPresent(message = "A data de nascimento não pode ser futura")
    private LocalDate dataNascimento;

    // NEEDLESS?
    private LocalDate dataCriacao;
    private LocalDate dataEdicao;

}
