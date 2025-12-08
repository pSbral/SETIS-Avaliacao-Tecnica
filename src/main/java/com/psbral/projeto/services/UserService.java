package com.psbral.projeto.services;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.models.User;
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
    @Override
    @Transactional
    public UserDTO.Response insert(UserDTO.Request dto) {

        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + dto.email());
        }

        User entity = modelMapper.map(dto, User.class);
        entity.onCreate();
        User finalEntity = repository.save(entity);

        return modelMapper.map(finalEntity, UserDTO.Response.class);
    }


    // ALL
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.Response> findAll() {
        return repository.findAll().stream()
                .map(i -> modelMapper.map(i, UserDTO.Response.class))
                .collect(Collectors.toList());
    }

    // BY ID
    @Override
    @Transactional(readOnly = true)
    public UserDTO.Response findById(String id) {
        User entity = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Usuário não encontrado - id: " + id)
        );

        return modelMapper.map(entity, UserDTO.Response.class);
    }

    // UPDATE
    // COPY
    private void copyToUser(UserDTO.Request source, User target) {
        target.setName(source.name());
        target.setEmail(source.email());
        target.setBirthDate(source.birthDate());
    }

    @Override
    @Transactional
    public UserDTO.Response update(String id, UserDTO.Request entity) {
        try {

            User user = repository.getReferenceById(id);

            if (!user.getEmail().equals(entity.email())
                    && repository.existsByEmail(entity.email())) {

                throw new IllegalArgumentException("E-mail já cadastrado: " + entity.email());
            }

            copyToUser(entity, user);
            user = repository.save(user);
            return modelMapper.map(user, UserDTO.Response.class);

        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Usuário não encontrado - id: " + id);
        }
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
