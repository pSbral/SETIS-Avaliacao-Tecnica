package com.psbral.projeto.services;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.models.User;
import com.psbral.projeto.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserService implements ServiceRepository {

    private final UserRepository repository;

    // CREATE
    @Override
    @Transactional
    public UserDTO.Response insert(UserDTO.Request dto) {

        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + dto.email());
        }

        User entity = new User();
        copyToUser(dto, entity);          // preenche name, email, birthDate

        User saved = repository.save(entity);

        return toResponse(saved);         // monta o DTO de resposta
    }

    // READ – FIND ALL
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.Response> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // READ – FIND BY ID
    @Override
    @Transactional(readOnly = true)
    public UserDTO.Response findById(String id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado - id: " + id
                ));

        return toResponse(user);
    }

    // UPDATE
    @Override
    @Transactional
    public UserDTO.Response update(String id, UserDTO.Request dto) {

        User entity = repository.getReferenceById(id);

        if (!entity.getEmail().equals(dto.email())
                && repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + dto.email());
        }

        copyToUser(dto, entity);

        User saved = repository.save(entity);

        return toResponse(saved);
    }

    private void copyToUser(UserDTO.Request source, User target) {
        target.setName(source.name());
        target.setEmail(source.email());
        target.setBirthDate(source.birthDate());
        // createdAt e lastUpdate continuam sendo controlados pelo @PrePersist / @PreUpdate
    }

    private UserDTO.Response toResponse(User u) {
        return new UserDTO.Response(
                u.getName(),
                u.getEmail(),
                u.getBirthDate()
        );
    }

    // DELETE
    @Override
    @Transactional
    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado - id: " + id);
        }

        try {
            repository.deleteById(id);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Falha de integridade referencial - id: " + id);
        }
    }
}
