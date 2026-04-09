package com.example.moral.controller;

import com.example.moral.entity.User;
import com.example.moral.entity.Role;
import com.example.moral.entity.Student;
import com.example.moral.repository.UserRepository;
import com.example.moral.repository.StudentRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AuthController(AuthService authService, UserRepository userRepository, StudentRepository studentRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        var userOptional = authService.authenticate(username, password);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名或密码错误");
        }
        var user = userOptional.get();
        String token = authService.createToken(user);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        result.put("displayName", user.getDisplayName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.invalidate(authorization.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "注销成功"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestHeader HttpHeaders headers, @RequestBody User user) {
        User currentUser = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.TEACHER) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "只有管理员或教师可以注册新用户");
        }
        if (user.getRole() != Role.STUDENT && user.getRole() != Role.CLASS_CADRE) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "只能注册学生或班干部账户");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("用户名已存在");
        }
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/import-students")
    public ResponseEntity<?> importStudents(@RequestHeader HttpHeaders headers,
                                            @RequestBody List<StudentImportRequest> requests) {
        User currentUser = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.TEACHER) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "只有管理员或教师可以导入学生账号");
        }
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest().body("导入列表不能为空");
        }

        Set<String> studentNumbers = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        for (StudentImportRequest request : requests) {
            if (request.studentNumber == null || request.studentNumber.isBlank() || request.name == null || request.name.isBlank()) {
                return ResponseEntity.badRequest().body("每个学生必须包含学号和姓名");
            }
            String username = request.studentNumber.trim();
            if (request.password == null || request.password.isBlank()) {
                return ResponseEntity.badRequest().body("每个学生必须包含密码");
            }
            if (!studentNumbers.add(username)) {
                return ResponseEntity.badRequest().body("导入列表中存在重复学号: " + username);
            }
            if (!usernames.add(username)) {
                return ResponseEntity.badRequest().body("导入列表中存在重复用户名: " + username);
            }
            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body("用户名已存在: " + username);
            }
            if (studentRepository.findByStudentNumber(username).isPresent()) {
                return ResponseEntity.badRequest().body("学号已存在: " + username);
            }
        }

        for (StudentImportRequest request : requests) {
            String username = request.studentNumber.trim();
            Role role = Role.STUDENT;
            if (request.role != null && !request.role.isBlank()) {
                try {
                    role = Role.valueOf(request.role.trim());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("无效角色: " + request.role);
                }
            }
            if (role != Role.STUDENT && role != Role.CLASS_CADRE) {
                return ResponseEntity.badRequest().body("导入账号只支持 STUDENT 或 CLASS_CADRE 角色");
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(request.password.trim());
            user.setRole(role);
            user.setDisplayName(request.displayName == null || request.displayName.isBlank() ? request.name.trim() : request.displayName.trim());
            userRepository.save(user);

            Student student = new Student();
            student.setStudentNumber(username);
            student.setName(request.name.trim());
            student.setGrade(request.grade == null ? "" : request.grade.trim());
            student.setClassName(request.className == null ? "" : request.className.trim());
            student.setTotalScore(0);
            studentRepository.save(student);
        }

        return ResponseEntity.ok(Map.of("imported", requests.size()));
    }

    public static class StudentImportRequest {
        public String studentNumber;
        public String name;
        public String displayName;
        public String password;
        public String role;
        public String grade;
        public String className;
    }
}
