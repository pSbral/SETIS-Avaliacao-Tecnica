package com.psbral.projeto.controllers;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.models.User;
import com.psbral.projeto.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService service;

    // CREATE
    // POST /users
    @PostMapping
    public ResponseEntity<UserDTO> insert(@RequestBody @Valid UserDTO dto){
        dto = service.insert(dto);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    // READ – FIND ALL
    // GET /users
    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        List<UserDTO> dto = service.findAll();
        return ResponseEntity.ok(dto);
    }

    // READ – FIND BY ID
    // GET /users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable @NotNull Long id) {
        UserDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    // UPDATE
    // PUT /users/{id}
    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id,
                       @Valid @RequestBody UserDTO user) {
        return service.update(id, user);
    }

    // DELETE
    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NotNull Long id) {
        service.delete(id);
    }
}
