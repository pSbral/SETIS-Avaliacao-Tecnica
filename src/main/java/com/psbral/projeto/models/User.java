package com.psbral.projeto.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
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
    @Column(length = 26, nullable = false, updatable = false)
    private String id;

	@Column(length = 50, nullable = false)
	private String name;

    @Column(length = 254, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    @PrePersist
    public void onCreate() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.createdAt = LocalDateTime.now();
    }
}
