package com.example.moral.controller;

import com.example.moral.entity.Role;
import com.example.moral.entity.Teacher;
import com.example.moral.entity.User;
import com.example.moral.repository.TeacherRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherRepository repository;
    private final AuthService authService;

    public TeacherController(TeacherRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @GetMapping
    public List<Teacher> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Teacher> addTeacher(@RequestHeader HttpHeaders headers,
                                              @RequestBody Teacher teacher) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return ResponseEntity.ok(repository.save(teacher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@RequestHeader HttpHeaders headers,
                                                 @PathVariable Long id,
                                                 @RequestBody Teacher payload) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return repository.findById(id)
                .map(teacher -> {
                    teacher.setName(payload.getName());
                    teacher.setEmail(payload.getEmail());
                    teacher.setPhone(payload.getPhone());
                    return ResponseEntity.ok(repository.save(teacher));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(@RequestHeader HttpHeaders headers,
                                           @PathVariable Long id) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return repository.findById(id)
                .map(teacher -> {
                    repository.delete(teacher);
                    return ResponseEntity.noContent().build();
                }).orElse(ResponseEntity.notFound().build());
    }
}
