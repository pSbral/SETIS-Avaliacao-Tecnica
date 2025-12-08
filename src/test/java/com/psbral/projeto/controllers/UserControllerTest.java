package com.psbral.projeto.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.services.ServiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceRepository service;

    @Autowired
    private ObjectMapper objectMapper;

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

    private UserDTO.Response buildResponse(String name, String email) {
        return new UserDTO.Response(
                name,
                email,
                LocalDate.of(2000, 1, 1)
        );
    }

    // POST /users - INSERT
    @Test
    void insert_shouldReturnCreated_whenValidRequest() throws Exception {
        UserDTO.Response saved = buildResponse("Fulano", "fulano@email.com");
        when(service.insert(any(UserDTO.Request.class))).thenReturn(saved);

        UserDTO.Request requestBody = buildRequest("00H00000000000000000000001",
                "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                // não valida Location de propósito
                .andExpect(jsonPath("$.name").value("Fulano"))
                .andExpect(jsonPath("$.email").value("fulano@email.com"));
    }

    @Test
    void insert_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        when(service.insert(any(UserDTO.Request.class)))
                .thenThrow(new IllegalArgumentException("E-mail already exists"));

        UserDTO.Request requestBody = buildRequest("00H00000000000000000000001",
                "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.value").value(400))
                .andExpect(jsonPath("$.message").value("E-mail already exists"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void insert_shouldReturnBadRequest_whenValidationFails() throws Exception {
        String invalidJson = """
                {
                  "id": 0,
                  "name": "Fulano",
                  "email": "notavalidformat",
                  "birthDate": "2009-01-01"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.value").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message",
                        containsString("email: Formato de e-mail inválido")));
    }

    @Test
    void insert_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        when(service.insert(any(UserDTO.Request.class)))
                .thenThrow(new RuntimeException("Database down"));

        UserDTO.Request requestBody = buildRequest("00H00000000000000000000001",
                "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.value").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    // GET /users - FIND ALL
    @Test
    void findAll_shouldReturnOkWithList_whenUsersExist() throws Exception {
        List<UserDTO.Response> list = List.of(
                buildResponse("Fulano", "f1@email.com"),
                buildResponse("Ciclano", "f2@email.com")
        );

        when(service.findAll()).thenReturn(list);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Fulano"))
                .andExpect(jsonPath("$[0].email").value("f1@email.com"))
                .andExpect(jsonPath("$[1].name").value("Ciclano"))
                .andExpect(jsonPath("$[1].email").value("f2@email.com"));
    }

    @Test
    void findAll_shouldReturnNoContent_whenNoUsers() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isNoContent());
    }

    // GET /users/{id} - FIND BY ID
    @Test
    void findById_shouldReturnOk_whenUserExists() throws Exception {
        String id = "01H00000000000000000000001";
        UserDTO.Response response = buildResponse("Fulano", "fulano@email.com");

        when(service.findById(id)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fulano"))
                .andExpect(jsonPath("$.email").value("fulano@email.com"));
    }

    @Test
    void findById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String id = "01H00000000000000000000001";
        String msg = "Usuário não encontrado - id: " + id;

        when(service.findById(id))
                .thenThrow(new EntityNotFoundException(msg));

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.value").value(404))
                .andExpect(jsonPath("$.message").value(msg))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }

    // PUT /users/{id} - UPDATE
    @Test
    void update_shouldReturnOk_whenValidRequest() throws Exception {
        String id = "01H00000000000000000000001";
        UserDTO.Response updated = buildResponse("Novo Nome", "novo@email.com");

        when(service.update(eq(id), any(UserDTO.Request.class))).thenReturn(updated);

        UserDTO.Request requestBody = buildRequest(id, "Novo Nome", "novo@email.com");

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo Nome"))
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    @Test
    void update_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        String id = "01H00000000000000000000001";

        when(service.update(eq(id), any(UserDTO.Request.class)))
                .thenThrow(new IllegalArgumentException("E-mail already exists"));

        UserDTO.Request requestBody = buildRequest(id, "Fulano", "duplicado@email.com");

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.value").value(400))
                .andExpect(jsonPath("$.message").value("E-mail already exists"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }

    @Test
    void update_shouldReturnBadRequest_whenValidationFails() throws Exception {
        String id = "01H00000000000000000000001";

        String invalidJson = """
                {
                  "id": "01H00000000000000000000001",
                  "name": "Fulano",
                  "email": "notavalidformat",
                  "birthDate": "2009-01-01"
                }
                """;

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.value").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message",
                        containsString("email: Formato de e-mail inválido")));
    }

    @Test
    void update_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String id = "01H00000000000000000000001";
        String msg = "Usuário não encontrado - id: " + id;

        when(service.update(eq(id), any(UserDTO.Request.class)))
                .thenThrow(new EntityNotFoundException(msg));

        UserDTO.Request requestBody = buildRequest(id, "Fulano", "fulano@email.com");

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.value").value(404))
                .andExpect(jsonPath("$.message").value(msg))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }

    @Test
    void update_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        String id = "01H00000000000000000000001";

        when(service.update(eq(id), any(UserDTO.Request.class)))
                .thenThrow(new RuntimeException("Database down"));

        UserDTO.Request requestBody = buildRequest(id, "Fulano", "fulano@email.com");

        mockMvc.perform(put("/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.value").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }

    // DELETE /users/{id} - DELETE
    @Test
    void delete_shouldReturnOk_whenUserExists() throws Exception {
        String id = "01H00000000000000000000001";

        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        String id = "01H00000000000000000000001";
        String msg = "Usuário não encontrado - id: " + id;

        doThrow(new EntityNotFoundException(msg))
                .when(service).delete(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.value").value(404))
                .andExpect(jsonPath("$.message").value(msg))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }

    @Test
    void delete_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        String id = "01H00000000000000000000001";

        doThrow(new RuntimeException("Database down"))
                .when(service).delete(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.value").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/users/" + id));
    }
}
