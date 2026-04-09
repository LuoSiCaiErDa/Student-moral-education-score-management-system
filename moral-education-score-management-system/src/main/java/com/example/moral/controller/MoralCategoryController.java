package com.example.moral.controller;

import com.example.moral.entity.MoralCategory;
import com.example.moral.entity.Role;
import com.example.moral.entity.User;
import com.example.moral.repository.MoralCategoryRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class MoralCategoryController {
    private final MoralCategoryRepository repository;
    private final AuthService authService;

    public MoralCategoryController(MoralCategoryRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @GetMapping
    public List<MoralCategory> getCategories() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<MoralCategory> createCategory(@RequestHeader HttpHeaders headers,
                                                         @RequestBody MoralCategory category) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return ResponseEntity.ok(repository.save(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MoralCategory> updateCategory(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody MoralCategory payload) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return repository.findById(id)
                .map(category -> {
                    category.setName(payload.getName());
                    category.setDescription(payload.getDescription());
                    return ResponseEntity.ok(repository.save(category));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@RequestHeader HttpHeaders headers,
                                           @PathVariable Long id) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return repository.findById(id)
                .map(category -> {
                    repository.delete(category);
                    return ResponseEntity.noContent().build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
