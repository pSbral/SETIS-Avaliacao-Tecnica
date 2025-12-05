package com.psbral.projeto.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceRepository service;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO buildDTO(long id, String name, String email) {
        return new UserDTO(
                id,
                name,
                email,
                LocalDate.of(2000, 1, 1),
                null,
                null
        );
    }

    @Test
    void insert_shouldReturnCreated_whenValidRequest() throws Exception {
        UserDTO saved = buildDTO(1L, "Fulano", "fulano@email.com");
        when(service.insert(any(UserDTO.class))).thenReturn(saved);

        UserDTO requestBody = buildDTO(0L, "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/users/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fulano"))
                .andExpect(jsonPath("$.email").value("fulano@email.com"));
    }

    @Test
    void insert_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        when(service.insert(any(UserDTO.class)))
                .thenThrow(new IllegalArgumentException("E-mail already exists"));

        UserDTO requestBody = buildDTO(0L, "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                // campos do ApiError
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
                // o handler de MethodArgumentNotValidException monta um ApiError
                .andExpect(jsonPath("$.value").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message", containsString("email: Formato de e-mail inválido")));
    }

    @Test
    void insert_shouldReturnInternalServerError_whenUnexpectedException() throws Exception {
        when(service.insert(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Database down"));

        UserDTO requestBody = buildDTO(0L, "Fulano", "fulano@email.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.value").value(500))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.path").value("/users"));
    }
}
