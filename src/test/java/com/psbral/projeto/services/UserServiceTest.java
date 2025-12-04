package com.psbral.projeto.services;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.models.User;
import com.psbral.projeto.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    private UserService service;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        service = new UserService(repository, modelMapper);
    }

    private User buildUser(Long id, String nome, String email) {
        User u = new User();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setDataNascimento(LocalDate.of(2000, 1, 1));
        return u;
    }

    private UserDTO buildDTO(Long id, String nome, String email) {
        UserDTO dto = new UserDTO();
        dto.setId(id);
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setDataNascimento(LocalDate.of(2000, 1, 1));
        return dto;
    }

    // INSERT
    @Test
    void insert_sucess() {
        UserDTO dto = buildDTO(1L, "Fulano", "fulano@email.com");

        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(repository.existsByEmail("fulano@email.com"))
                .thenReturn(false);

        UserDTO result = service.insert(dto);

        assertEquals("Fulano", result.getNome());
        assertEquals("fulano@email.com", result.getEmail());
        verify(repository).save(any(User.class));
        verify(repository).existsByEmail(result.getEmail());
    }

    @Test
    void insert_emailAlreadyExists() {
        UserDTO dto = buildDTO(1L, "Fulano", "email@jaexiste.com");

        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(repository.existsByEmail(anyString()))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.insert(dto)
        );

        assertTrue(ex.getMessage().contains("E-mail já cadastrado"));
        verify(repository).existsByEmail(anyString());
    }

    // FIND ALL
    @Test
    void findAll_sucess() {
        User u1 = buildUser(1L, "Fulano", "f1@email.com");
        User u2 = buildUser(2L, "Ciclano", "f2@email.com");

        when(repository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserDTO> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Fulano", result.get(0).getNome());
        assertEquals("Ciclano", result.get(1).getNome());
        verify(repository).findAll();
    }

    // FIND BY ID
    @Test
    void findById_sucess() {
        User u = buildUser(1L, "Fulano", "fulano@email.com");
        when(repository.findById(1L)).thenReturn(Optional.of(u));

        UserDTO result = service.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Fulano", result.getNome());
        verify(repository).findById(1L);
    }

    @Test
    void findById_idNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.findById(1L)
        );

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
        verify(repository).findById(1L);
    }

    // UPDATE

    @Test
    void update_sucess() {
        UserDTO dto = buildDTO(1L, "Novo Nome", "novo@email.com");
        User existing = buildUser(1L, "Antigo Nome", "antigo@email.com");

        when(repository.getReferenceById(1L)).thenReturn(existing);
        when(repository.existsByEmail("novo@email.com"))
                .thenReturn(false);
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = service.update(1L, dto);

        assertEquals(1L, result.getId());
        assertEquals("Novo Nome", result.getNome());
        assertEquals("novo@email.com", result.getEmail());
        verify(repository).getReferenceById(1L);
        verify(repository).save(existing);
    }

    @Test
    void update_emailAlreadyExists() {
        UserDTO dto = buildDTO(2L, "Fulano", "duplicado@email.com");
        User existing = buildUser(1L, "Fulano", "antigo@email.com");

        when(repository.getReferenceById(1L)).thenReturn(existing);
        when(repository.existsByEmail("duplicado@email.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1L, dto)
        );

        assertTrue(ex.getMessage().contains("E-mail já cadastrado"));
        verify(repository).existsByEmail(dto.getEmail());
    }

    @Test
    void update_idNotFound() {
        UserDTO dto = buildDTO(2L, "Fulano", "email@email.com");

        when(repository.getReferenceById(1L))
                .thenThrow(new EntityNotFoundException());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.update(1L, dto)
        );

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
        verify(repository).getReferenceById(1L);
    }

    // DELETE

    @Test
    void delete_sucess() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_idNotFound() {
        when(repository.existsById(1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(1L)
        );

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
        verify(repository).existsById(1L);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void delete_referenceIntegrityFail() {
        when(repository.existsById(1L)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("erro"))
                .when(repository).deleteById(1L);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(1L)
        );

        assertTrue(ex.getMessage().contains("Falha de integridade referencial"));
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }
}
