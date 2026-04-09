package com.example.moral.controller;

import com.example.moral.entity.User;
import com.example.moral.entity.Role;
import com.example.moral.entity.Student;
import com.example.moral.repository.UserRepository;
import com.example.moral.repository.StudentRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import com.example.moral.util.ResponseUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }
        var userOptional = authService.authenticate(username.trim(), password.trim());
        if (userOptional.isEmpty()) {
            return ResponseUtils.badRequest("用户名或密码错误");
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
        SecurityUtils.requireAnyRole(currentUser, Role.ADMIN, Role.TEACHER);
        if (user.getUsername() == null || user.getUsername().isBlank() || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
        }
        if (user.getRole() != Role.STUDENT && user.getRole() != Role.CLASS_CADRE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只能注册学生或班干部账户");
        }
        String username = user.getUsername().trim();
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseUtils.badRequest("用户名已存在");
        }
        user.setUsername(username);
        user.setPassword(user.getPassword().trim());
        userRepository.save(user);
        ensureStudentRecord(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/import-students")
    public ResponseEntity<?> importStudents(@RequestHeader HttpHeaders headers,
                                            @RequestBody List<StudentImportRequest> requests) {
        User currentUser = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有管理员或教师可以导入学生账号");
        }
        if (requests == null || requests.isEmpty()) {
            return ResponseUtils.badRequest("导入列表不能为空");
        }

        Set<String> studentNumbers = new HashSet<>();
        Set<String> usernames = new HashSet<>();
        for (StudentImportRequest request : requests) {
            if (request.studentNumber == null || request.studentNumber.isBlank() || request.name == null || request.name.isBlank()) {
                return ResponseUtils.badRequest("每个学生必须包含学号和姓名");
            }
            String username = request.studentNumber.trim();
            if (request.password == null || request.password.isBlank()) {
                return ResponseUtils.badRequest("每个学生必须包含密码");
            }
            if (!studentNumbers.add(username)) {
                return ResponseUtils.badRequest("导入列表中存在重复学号: " + username);
            }
            if (!usernames.add(username)) {
                return ResponseUtils.badRequest("导入列表中存在重复用户名: " + username);
            }
            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseUtils.badRequest("用户名已存在: " + username);
            }
            if (studentRepository.findByStudentNumber(username).isPresent()) {
                return ResponseUtils.badRequest("学号已存在: " + username);
            }
        }

        for (StudentImportRequest request : requests) {
            String username = request.studentNumber.trim();
            Role role = Role.STUDENT;
            if (request.role != null && !request.role.isBlank()) {
                try {
                    role = Role.valueOf(request.role.trim());
                } catch (IllegalArgumentException e) {
                    return ResponseUtils.badRequest("无效角色: " + request.role);
                }
            }
            if (role != Role.STUDENT && role != Role.CLASS_CADRE) {
                return ResponseUtils.badRequest("导入账号只支持 STUDENT 或 CLASS_CADRE 角色");
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

    private void ensureStudentRecord(User user) {
        if (user.getRole() != Role.STUDENT && user.getRole() != Role.CLASS_CADRE) {
            return;
        }
        if (studentRepository.findByStudentNumber(user.getUsername()).isPresent()) {
            return;
        }
        Student student = new Student();
        student.setStudentNumber(user.getUsername());
        student.setName(user.getDisplayName() == null || user.getDisplayName().isBlank() ? user.getUsername() : user.getDisplayName());
        student.setGrade("");
        student.setClassName("");
        student.setTotalScore(0);
        studentRepository.save(student);
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
