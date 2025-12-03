package com.psbral.projeto.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tb_usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = "email")
})
public class User {
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_seq", allocationSize = 1)
    private Long id;

	@Column(length = 50, nullable = false)
	private String nome;

    @Column(length = 254, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private LocalDateTime dataEdicao;

    @PrePersist
    public void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
        this.dataEdicao = LocalDateTime.now();
    }

    @PreUpdate
    public void aoEditar() {
        this.dataEdicao = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", dataCriacao=" + dataCriacao +
                ", dataEdicao=" + dataEdicao +
                '}';
    }
}
