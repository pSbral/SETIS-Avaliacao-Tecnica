package com.psbral.projeto.controllers;

import com.psbral.projeto.dto.UserDTO;
import com.psbral.projeto.services.ServiceRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final ServiceRepository service;

    // CREATE
    @PostMapping
    public ResponseEntity<UserDTO.Response> insert(@RequestBody @Valid UserDTO.Request dto){
        UserDTO.Response saved = service.insert(dto);
        return ResponseEntity.status(201).body(saved);
    }


    // READ – FIND ALL
    @GetMapping
    public ResponseEntity<List<UserDTO.Response>> findAll() {
        List<UserDTO.Response> dto = service.findAll();
        if (dto.isEmpty()) {
            return ResponseEntity.noContent().build();   // 204 :)
        }
        return ResponseEntity.ok(dto);
    }

    // READ – FIND BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.Response> findById(@PathVariable @NotNull String id) {
        UserDTO.Response dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    // UPDATE
    @PutMapping("/{id}")
    public UserDTO.Response update(@PathVariable String id,
                          @Valid @RequestBody UserDTO.Request user) {
        return service.update(id, user);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable @NotNull String id) {
        service.delete(id);
    }
}
