package com.psbral.projeto.services;

import com.psbral.projeto.models.User;
import com.psbral.projeto.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setNome("Joao Silva");
        user.setEmail("joao@example.com");
        user.setDataNascimento(LocalDate.of(1990, 1, 1));
    }

    // INSERT

    @Test
    void insert_DeveSalvarQuandoEmailNaoExiste() {
        when(repository.existsByEmail(user.getEmail())).thenReturn(false);
        when(repository.save(user)).thenReturn(user);

        User resultado = service.insert(user);

        assertNotNull(resultado);
        assertEquals(user.getEmail(), resultado.getEmail());
        verify(repository).existsByEmail(user.getEmail());
        verify(repository).save(user);
    }

    @Test
    void insert_DeveLancarExceptionQuandoEmailJaExiste() {
        when(repository.existsByEmail(user.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.insert(user)
        );

        assertTrue(ex.getMessage().contains("E-mail já cadastrado"));
        verify(repository).existsByEmail(user.getEmail());
        verify(repository, never()).save(any());
    }

    // FIND ALL

    @Test
    void findAll_DeveRetornarListaDeUsuarios() {
        when(repository.findAll()).thenReturn(Arrays.asList(user));

        List<User> lista = service.findAll();

        assertNotNull(lista);
        assertEquals(1, lista.size());
        verify(repository).findAll();
    }

    // FIND BY ID

    @Test
    void findById_DeveRetornarUsuarioQuandoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(repository).findById(1L);
    }

    @Test
    void findById_DeveLancarExceptionQuandoNaoExiste() {
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
    void update_DeveAtualizarQuandoUsuarioExisteEEmailNaoConflita() {
        User atualizado = new User();
        atualizado.setNome("Nome Atualizado");
        atualizado.setEmail("joao@example.com");
        atualizado.setDataNascimento(LocalDate.of(1991, 2, 2));

        when(repository.getReferenceById(1L)).thenReturn(user);
        when(repository.save(any(User.class))).thenReturn(user);

        User resultado = service.update(1L, atualizado);

        assertNotNull(resultado);
        assertEquals("Nome Atualizado", user.getNome());
        verify(repository).getReferenceById(1L);
        verify(repository, never()).existsByEmail(anyString());
        verify(repository).save(user);
    }

    @Test
    void update_DeveLancarExceptionQuandoEmailJaExisteEmOutroUsuario() {
        User atualizado = new User();
        atualizado.setNome("Outro Nome");
        atualizado.setEmail("novoemail@example.com");

        when(repository.getReferenceById(1L)).thenReturn(user);
        when(repository.existsByEmail(atualizado.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1L, atualizado)
        );

        assertTrue(ex.getMessage().contains("E-mail já cadastrado"));
        verify(repository).getReferenceById(1L);
        verify(repository).existsByEmail(atualizado.getEmail());
        verify(repository, never()).save(any());
    }

    @Test
    void update_DeveLancarExceptionQuandoUsuarioNaoExiste() {
        User atualizado = new User();
        atualizado.setNome("Teste");
        atualizado.setEmail("teste@example.com");

        when(repository.getReferenceById(1L)).thenThrow(new EntityNotFoundException());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(1L, atualizado)
        );

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
        verify(repository).getReferenceById(1L);
    }

    // DELETE

    @Test
    void delete_DeveExcluirQuandoUsuarioExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_DeveLancarExceptionQuandoUsuarioNaoExiste() {
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
    void delete_DeveLancarExceptionQuandoHaViolacaoDeIntegridade() {
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
