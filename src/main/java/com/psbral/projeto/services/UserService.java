package com.psbral.projeto.services;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.models.User;
import com.psbral.projeto.repository.ServiceRepository;
import com.psbral.projeto.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserService implements ServiceRepository {

    private final UserRepository repository;
    private final ModelMapper modelMapper;

    // CREATE
    @Transactional
    public UserDTO insert(UserDTO dto) {
        User entity = modelMapper.map(dto, User.class);
        entity.aoCriar();
        User finalEntity = repository.save(entity);

        if (repository.existsByEmail(finalEntity.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + finalEntity.getEmail());
        }

        return modelMapper.map(finalEntity, UserDTO.class);
    }

    // ALL
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return repository.findAll().stream()
                .map(i -> modelMapper.map(i, UserDTO.class))
                .collect(Collectors.toList());
    }

    // BY ID
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User entity = repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Usuário não encontrado - id: " + id)
        );

        return modelMapper.map(entity, UserDTO.class);
    }

    // UPDATE
    // COPY
    private void copyToUser(UserDTO source, User target) {
        target.setNome(source.name());
        target.setEmail(source.email());
        target.setDataNascimento(source.birthDate());
    }

    @Transactional
    public UserDTO update(Long id, UserDTO entity) {
        try {

            User user = repository.getReferenceById(id);

            if (!user.getEmail().equals(entity.email())
                    && repository.existsByEmail(entity.email())) {

                throw new IllegalArgumentException("E-mail já cadastrado: " + entity.email());
            }

            copyToUser(entity, user);
            user = repository.save(user);
            return modelMapper.map(user, UserDTO.class);

        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Usuário não encontrado - id: " + id);
        }
    }

    // DELETE
    @Transactional
    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado - id: " + id);
        }

        try {
            repository.deleteById(id);

        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Falha de integridade referencial - id: " + id);
        }
    }
}
