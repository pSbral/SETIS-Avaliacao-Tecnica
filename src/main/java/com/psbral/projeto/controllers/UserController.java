package com.psbral.projeto.controllers;

import com.psbral.projeto.models.User;
import com.psbral.projeto.services.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    // CREATE
    // POST /users
    @PostMapping
    @Transactional
    public User insert(@Valid @RequestBody User user) {
        return service.insert(user);
    }

    // READ – FIND ALL
    // GET /users
    @GetMapping
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return service.findAll();
    }

    // READ – FIND BY ID
    // GET /users/{id}
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public User findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // UPDATE
    // PUT /users/{id}
    @PutMapping("/{id}")
    @Transactional
    public User update(@PathVariable Long id,
                       @Valid @RequestBody User user) {
        return service.update(id, user);
    }

    // DELETE
    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
