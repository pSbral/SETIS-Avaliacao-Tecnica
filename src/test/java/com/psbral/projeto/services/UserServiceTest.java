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
import java.time.LocalDateTime;
import java.util.Arrays;
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

        // Mapeamento DTO -> Entity
        lenient().when(modelMapper.map(any(UserDTO.class), eq(User.class)))
                .thenAnswer(invocation -> {
                    UserDTO dto = invocation.getArgument(0);
                    User u = new User();
                    u.setId(dto.id());
                    u.setName(dto.name());
                    u.setEmail(dto.email());
                    u.setBirthDate(dto.birthDate());
                    if (dto.createdAt() != null) {
                        u.setCreatedAt(dto.createdAt().atStartOfDay());
                    }
                    if (dto.lastUpdate() != null) {
                        u.setLastUpdate(dto.lastUpdate().atStartOfDay());
                    }
                    return u;
                });

        // Mapeamento Entity -> DTO
        lenient().when(modelMapper.map(any(User.class), eq(UserDTO.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    LocalDateTime criacao = u.getCreatedAt();
                    LocalDateTime edicao  = u.getLastUpdate();

                    return new UserDTO(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getBirthDate(),
                            criacao != null ? criacao.toLocalDate() : null,
                            edicao != null ? edicao.toLocalDate() : null
                    );
                });
    }

    private User buildUser(String id, String nome, String email) {
        User u = new User();
        u.setId(id);
        u.setName(nome);
        u.setEmail(email);
        u.setBirthDate(LocalDate.of(2000, 1, 1));
        return u;
    }

    private UserDTO buildDTO(String id, String nome, String email) {
        return new UserDTO(
                id,
                nome,
                email,
                LocalDate.of(2000, 1, 1),
                null,
                null
        );
    }


    // INSERT

    @Test
    void insert_sucess() {
        UserDTO dto = buildDTO("00H00000000000000000000001", "Fulano", "fulano@email.com");
        User saved = buildUser("01H00000000000000000000001", "Fulano", "fulano@email.com");

        // Aceita qualquer String (inclusive null) para evitar PotentialStubbingProblem
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(saved);
        when(modelMapper.map(saved, UserDTO.class))
                .thenReturn(buildDTO("01H00000000000000000000001", "Fulano", "fulano@email.com"));

        UserDTO result = service.insert(dto);

        assertEquals("Fulano", result.name());
        assertEquals("fulano@email.com", result.email());
        assertEquals("01H00000000000000000000001", result.id());
        verify(repository).existsByEmail(any());
        verify(repository).save(any(User.class));
    }

    @Test
    void insert_emailAlreadyExists() {
        UserDTO dto = buildDTO("00H00000000000000000000001", "Fulano", "email@jaexiste.com");

        when(repository.existsByEmail(any())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.insert(dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("e-mail"));
        verify(repository).existsByEmail(any());
        verify(repository, never()).save(any());
    }

    // FIND ALL

    @Test
    void findAll_sucess() {
        User u1 = buildUser("01H00000000000000000000001", "Fulano", "f1@email.com");
        User u2 = buildUser("02H00000000000000000000001", "Ciclano", "f2@email.com");

        when(repository.findAll()).thenReturn(Arrays.asList(u1, u2));
        when(modelMapper.map(u1, UserDTO.class))
                .thenReturn(buildDTO("01H00000000000000000000001", "Fulano", "f1@email.com"));
        when(modelMapper.map(u2, UserDTO.class))
                .thenReturn(buildDTO("02H00000000000000000000001", "Ciclano", "f2@email.com"));

        List<UserDTO> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Fulano", result.get(0).name());
        assertEquals("Ciclano", result.get(1).name());
        verify(repository).findAll();
    }

    // FIND BY ID

    @Test
    void findById_sucess() {
        User u = buildUser("01H00000000000000000000001", "Fulano", "fulano@email.com");

        when(repository.findById("01H00000000000000000000001")).thenReturn(Optional.of(u));
        when(modelMapper.map(u, UserDTO.class))
                .thenReturn(buildDTO("01H00000000000000000000001", "Fulano", "fulano@email.com"));

        UserDTO result = service.findById("01H00000000000000000000001");

        assertEquals("01H00000000000000000000001", result.id());
        assertEquals("Fulano", result.name());
        verify(repository).findById("01H00000000000000000000001");
    }

    @Test
    void findById_idNotFound() {
        when(repository.findById("01H00000000000000000000001")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.findById("01H00000000000000000000001")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).findById("01H00000000000000000000001");
    }

    // UPDATE

    @Test
    void update_sucess() {
        UserDTO dto = buildDTO("01H00000000000000000000001", "Novo Nome", "novo@email.com");
        User existing = buildUser("01H00000000000000000000001", "Antigo Nome", "antigo@email.com");
        User updated  = buildUser("01H00000000000000000000001", "Novo Nome", "novo@email.com");

        when(repository.getReferenceById("01H00000000000000000000001")).thenReturn(existing);
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.save(existing)).thenReturn(updated);
        when(modelMapper.map(updated, UserDTO.class))
                .thenReturn(buildDTO("01H00000000000000000000001", "Novo Nome", "novo@email.com"));

        UserDTO result = service.update("01H00000000000000000000001", dto);

        assertEquals("01H00000000000000000000001", result.id());
        assertEquals("Novo Nome", result.name());
        assertEquals("novo@email.com", result.email());
        verify(repository).getReferenceById("01H00000000000000000000001");
        verify(repository).save(existing);
    }

    @Test
    void update_emailAlreadyExists() {
        UserDTO dto = buildDTO("01H00000000000000000000001", "Fulano", "duplicado@email.com");
        User existing = buildUser("01H00000000000000000000001", "Fulano", "antigo@email.com");

        when(repository.getReferenceById("01H00000000000000000000001")).thenReturn(existing);
        when(repository.existsByEmail(any())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update("01H00000000000000000000001", dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("e-mail"));
        verify(repository).existsByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void update_idNotFound() {
        UserDTO dto = buildDTO("01H00000000000000000000001", "Fulano", "email@email.com");

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
    void delete_sucess() {
        when(repository.existsById("01H00000000000000000000001")).thenReturn(true);
        doNothing().when(repository).deleteById("01H00000000000000000000001");

        service.delete("01H00000000000000000000001");

        verify(repository).existsById("01H00000000000000000000001");
        verify(repository).deleteById("01H00000000000000000000001");
    }

    @Test
    void delete_idNotFound() {
        when(repository.existsById("01H00000000000000000000001")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete("01H00000000000000000000001")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).existsById("01H00000000000000000000001");
        verify(repository, never()).deleteById(anyString());
    }

    @Test
    void delete_referenceIntegrityFail() {
        when(repository.existsById("01H00000000000000000000001")).thenReturn(true);
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
