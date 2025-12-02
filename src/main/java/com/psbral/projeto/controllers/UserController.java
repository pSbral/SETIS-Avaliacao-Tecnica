package com.psbral.projeto.controllers;

import com.psbral.projeto.models.User;
import com.psbral.projeto.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public User insert(@Valid @RequestBody User user) {
        return service.insert(user);
    }

    // READ – FIND ALL
    // GET /users
    @GetMapping
    public List<User> findAll() {
        return service.findAll();
    }

    // READ – FIND BY ID
    // GET /users/{id}
    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // UPDATE
    // PUT /users/{id}
    @PutMapping("/{id}")
    public User update(@PathVariable Long id,
                       @Valid @RequestBody User user) {
        return service.update(id, user);
    }

    // DELETE
    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
