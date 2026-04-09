package com.example.moral.controller;

import com.example.moral.entity.Role;
import com.example.moral.entity.User;
import com.example.moral.repository.UserRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public UserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<User> listUsers(@RequestHeader HttpHeaders headers) {
        var current = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(current, Role.ADMIN);
        return userRepository.findAll();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@RequestHeader HttpHeaders headers,
                                           @PathVariable Long id,
                                           @RequestBody Role newRole) {
        var current = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(current, Role.ADMIN);
        return userRepository.findById(id)
                .map(user -> {
                    user.setRole(newRole);
                    return ResponseEntity.ok(userRepository.save(user));
                }).orElse(ResponseEntity.notFound().build());
    }
}
