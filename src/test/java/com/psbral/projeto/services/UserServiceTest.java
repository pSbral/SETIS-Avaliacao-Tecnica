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
                    u.setNome(dto.name());
                    u.setEmail(dto.email());
                    u.setDataNascimento(dto.birthDate());
                    if (dto.createdAt() != null) {
                        u.setDataCriacao(dto.createdAt().atStartOfDay());
                    }
                    if (dto.lastUpdate() != null) {
                        u.setDataEdicao(dto.lastUpdate().atStartOfDay());
                    }
                    return u;
                });

        // Mapeamento Entity -> DTO
        lenient().when(modelMapper.map(any(User.class), eq(UserDTO.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    LocalDateTime criacao = u.getDataCriacao();
                    LocalDateTime edicao  = u.getDataEdicao();

                    return new UserDTO(
                            u.getId(),
                            u.getNome(),
                            u.getEmail(),
                            u.getDataNascimento(),
                            criacao != null ? criacao.toLocalDate() : null,
                            edicao != null ? edicao.toLocalDate() : null
                    );
                });
    }

    private User buildUser(Long id, String nome, String email) {
        User u = new User();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(email);
        u.setDataNascimento(LocalDate.of(2000, 1, 1));
        u.setDataCriacao(LocalDateTime.of(2024, 1, 1, 0, 0));
        u.setDataEdicao(LocalDateTime.of(2024, 1, 2, 0, 0));
        return u;
    }

    private UserDTO buildDTO(Long id, String nome, String email) {
        return new UserDTO(
                id,
                nome,
                email,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2)
        );
    }

    // INSERT

    @Test
    void insert_sucess() {
        UserDTO dto = buildDTO(0L, "Fulano", "fulano@email.com");
        User saved = buildUser(1L, "Fulano", "fulano@email.com");

        // Aceita qualquer String (inclusive null) para evitar PotentialStubbingProblem
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.save(any(User.class))).thenReturn(saved);
        when(modelMapper.map(saved, UserDTO.class))
                .thenReturn(buildDTO(1L, "Fulano", "fulano@email.com"));

        UserDTO result = service.insert(dto);

        assertEquals("Fulano", result.name());
        assertEquals("fulano@email.com", result.email());
        assertEquals(1L, result.id());
        verify(repository).existsByEmail(any());
        verify(repository).save(any(User.class));
    }

    @Test
    void insert_emailAlreadyExists() {
        UserDTO dto = buildDTO(0L, "Fulano", "email@jaexiste.com");

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
        User u1 = buildUser(1L, "Fulano", "f1@email.com");
        User u2 = buildUser(2L, "Ciclano", "f2@email.com");

        when(repository.findAll()).thenReturn(Arrays.asList(u1, u2));
        when(modelMapper.map(u1, UserDTO.class))
                .thenReturn(buildDTO(1L, "Fulano", "f1@email.com"));
        when(modelMapper.map(u2, UserDTO.class))
                .thenReturn(buildDTO(2L, "Ciclano", "f2@email.com"));

        List<UserDTO> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Fulano", result.get(0).name());
        assertEquals("Ciclano", result.get(1).name());
        verify(repository).findAll();
    }

    // FIND BY ID

    @Test
    void findById_sucess() {
        User u = buildUser(1L, "Fulano", "fulano@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(u));
        when(modelMapper.map(u, UserDTO.class))
                .thenReturn(buildDTO(1L, "Fulano", "fulano@email.com"));

        UserDTO result = service.findById(1L);

        assertEquals(1L, result.id());
        assertEquals("Fulano", result.name());
        verify(repository).findById(1L);
    }

    @Test
    void findById_idNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.findById(1L)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
        verify(repository).findById(1L);
    }

    // UPDATE

    @Test
    void update_sucess() {
        UserDTO dto = buildDTO(1L, "Novo Nome", "novo@email.com");
        User existing = buildUser(1L, "Antigo Nome", "antigo@email.com");
        User updated  = buildUser(1L, "Novo Nome", "novo@email.com");

        when(repository.getReferenceById(1L)).thenReturn(existing);
        when(repository.existsByEmail(any())).thenReturn(false);
        when(repository.save(existing)).thenReturn(updated);
        when(modelMapper.map(updated, UserDTO.class))
                .thenReturn(buildDTO(1L, "Novo Nome", "novo@email.com"));

        UserDTO result = service.update(1L, dto);

        assertEquals(1L, result.id());
        assertEquals("Novo Nome", result.name());
        assertEquals("novo@email.com", result.email());
        verify(repository).getReferenceById(1L);
        verify(repository).save(existing);
    }

    @Test
    void update_emailAlreadyExists() {
        UserDTO dto = buildDTO(1L, "Fulano", "duplicado@email.com");
        User existing = buildUser(1L, "Fulano", "antigo@email.com");

        when(repository.getReferenceById(1L)).thenReturn(existing);
        when(repository.existsByEmail(any())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1L, dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("e-mail"));
        verify(repository).existsByEmail(any());
        verify(repository, never()).save(any());
    }

    @Test
    void update_idNotFound() {
        UserDTO dto = buildDTO(1L, "Fulano", "email@email.com");

        when(repository.getReferenceById(1L))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado"));

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.update(1L, dto)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
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

        assertTrue(ex.getMessage().toLowerCase().contains("usuário"));
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

        assertTrue(ex.getMessage().toLowerCase().contains("integridade"));
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }
}
