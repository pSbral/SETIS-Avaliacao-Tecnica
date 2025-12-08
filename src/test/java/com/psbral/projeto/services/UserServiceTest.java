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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private ModelMapper modelMapper;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, modelMapper);

        lenient().when(modelMapper.map(any(UserDTO.Request.class), eq(User.class)))
                .thenAnswer(invocation -> {
                    UserDTO.Request dto = invocation.getArgument(0);
                    User u = new User();
                    u.setId(dto.id());
                    u.setName(dto.name());
                    u.setEmail(dto.email());
                    u.setBirthDate(dto.birthDate());
                    return u;
                });

        lenient().when(modelMapper.map(any(User.class), eq(UserDTO.Response.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    return new UserDTO.Response(
                            u.getName(),
                            u.getEmail(),
                            u.getBirthDate()
                    );
                });
    }

    private User buildUser(String id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setBirthDate(LocalDate.of(2000, 1, 1));
        return u;
    }

    private UserDTO.Request buildRequest(String id, String name, String email) {
        return new UserDTO.Request(
                id,
                name,
                email,
                LocalDate.of(2000, 1, 1),
                null,
                null
        );
    }

    // INSERT
    @Test
    void insert_shouldReturnResponse_whenValidRequest() {
        UserDTO.Request dto = buildRequest("00H00000000000000000000001",
                "Fulano", "fulano@email.com");
        User saved = buildUser("01H00000000000000000000001",
                "Fulano", "fulano@email.com");

        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(saved);

        UserDTO.Response result = service.insert(dto);

        assertEquals("Fulano", result.name());
        assertEquals("fulano@email.com", result.email());
        assertEquals(LocalDate.of(2000, 1, 1), result.birthDate());
        verify(repository).existsByEmail(dto.email());
        verify(repository).save(any(User.class));
    }

    @Test
    void insert_shouldThrowIllegalArgumentException_whenEmailAlreadyExists() {
        UserDTO.Request dto = buildRequest("00H00000000000000000000001",
                "Fulano", "email@jaexiste.com");

        when(repository.existsByEmail(dto.email())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.insert(dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("e-mail"));
        verify(repository).existsByEmail(dto.email());
        verify(repository, never()).save(any());
    }

    // FIND ALL
    @Test
    void findAll_shouldReturnListOfResponses_whenUsersExist() {
        User u1 = buildUser("01H00000000000000000000001",
                "Fulano", "f1@email.com");
        User u2 = buildUser("02H00000000000000000000001",
                "Ciclano", "f2@email.com");

        when(repository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserDTO.Response> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Fulano", result.get(0).name());
        assertEquals("Ciclano", result.get(1).name());
        verify(repository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoUsersExist() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<UserDTO.Response> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    // FIND BY ID
    @Test
    void findById_shouldReturnResponse_whenIdExists() {
        User u = buildUser("01H00000000000000000000001",
                "Fulano", "fulano@email.com");

        when(repository.findById("01H00000000000000000000001"))
                .thenReturn(Optional.of(u));

        UserDTO.Response result = service.findById("01H00000000000000000000001");

        assertEquals("Fulano", result.name());
        assertEquals("fulano@email.com", result.email());
        assertEquals(LocalDate.of(2000, 1, 1), result.birthDate());
        verify(repository).findById("01H00000000000000000000001");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenIdDoesNotExist() {
        when(repository.findById("01H00000000000000000000001"))
                .thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.findById("01H00000000000000000000001")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).findById("01H00000000000000000000001");
    }

    // UPDATE
    @Test
    void update_shouldReturnUpdatedResponse_whenValidRequestAndIdExists() {
        UserDTO.Request dto = buildRequest("01H00000000000000000000001",
                "Novo Nome", "novo@email.com");
        User existing = buildUser("01H00000000000000000000001",
                "Antigo Nome", "antigo@email.com");
        User updated = buildUser("01H00000000000000000000001",
                "Novo Nome", "novo@email.com");

        when(repository.getReferenceById("01H00000000000000000000001"))
                .thenReturn(existing);
        when(repository.existsByEmail(dto.email())).thenReturn(false);
        when(repository.save(existing)).thenReturn(updated);

        UserDTO.Response result = service.update("01H00000000000000000000001", dto);

        assertEquals("Novo Nome", result.name());
        assertEquals("novo@email.com", result.email());
        verify(repository).getReferenceById("01H00000000000000000000001");
        verify(repository).existsByEmail(dto.email());
        verify(repository).save(existing);
    }

    @Test
    void update_shouldThrowIllegalArgumentException_whenEmailAlreadyExists() {
        UserDTO.Request dto = buildRequest("01H00000000000000000000001",
                "Fulano", "duplicado@email.com");
        User existing = buildUser("01H00000000000000000000001",
                "Fulano", "antigo@email.com");

        when(repository.getReferenceById("01H00000000000000000000001"))
                .thenReturn(existing);
        when(repository.existsByEmail(dto.email())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update("01H00000000000000000000001", dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("e-mail"));
        verify(repository).existsByEmail(dto.email());
        verify(repository, never()).save(any());
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenIdDoesNotExist() {
        UserDTO.Request dto = buildRequest("01H00000000000000000000001",
                "Fulano", "email@email.com");

        when(repository.getReferenceById("01H00000000000000000000001"))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.update("01H00000000000000000000001", dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).getReferenceById("01H00000000000000000000001");
    }

    // DELETE
    @Test
    void delete_shouldDeleteUser_whenIdExists() {
        when(repository.existsById("01H00000000000000000000001"))
                .thenReturn(true);
        doNothing().when(repository)
                .deleteById("01H00000000000000000000001");

        service.delete("01H00000000000000000000001");

        verify(repository).existsById("01H00000000000000000000001");
        verify(repository).deleteById("01H00000000000000000000001");
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenIdDoesNotExist() {
        when(repository.existsById("01H00000000000000000000001"))
                .thenReturn(false);

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.delete("01H00000000000000000000001")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).existsById("01H00000000000000000000001");
        verify(repository, never()).deleteById(anyString());
    }

    @Test
    void delete_shouldThrowIllegalArgumentException_whenDataIntegrityViolationOccurs() {
        when(repository.existsById("01H00000000000000000000001"))
                .thenReturn(true);
        doThrow(new DataIntegrityViolationException("erro"))
                .when(repository).deleteById("01H00000000000000000000001");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete("01H00000000000000000000001")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("integridade"));
        verify(repository).existsById("01H00000000000000000000001");
        verify(repository).deleteById("01H00000000000000000000001");
    }
}
