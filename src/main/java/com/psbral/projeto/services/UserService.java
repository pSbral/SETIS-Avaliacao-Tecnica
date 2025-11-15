package com.psbral.projeto.services;

import com.psbral.projeto.models.User;
import com.psbral.projeto.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    // CREATE
    @Transactional
    public User insert(User user) {

        if (repository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + user.getEmail());
        }

        return repository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado - id: " + id));
    }

    private void copyToUser(User source, User target) {
        target.setNome(source.getNome());
        target.setEmail(source.getEmail());
        target.setDataNascimento(source.getDataNascimento());
    }

    @Transactional
    public User update(Long id, User entity) {
        try {

            User user = repository.getReferenceById(id);

            if (!user.getEmail().equals(entity.getEmail())
                    && repository.existsByEmail(entity.getEmail())) {

                throw new IllegalArgumentException("E-mail já cadastrado: " + entity.getEmail());
            }

            copyToUser(entity, user);

            user = repository.save(user);
            return user;

        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Usuário não encontrado - id: " + id);
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
