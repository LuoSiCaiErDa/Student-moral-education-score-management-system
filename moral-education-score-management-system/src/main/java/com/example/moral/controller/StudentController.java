package com.example.moral.controller;

import com.example.moral.entity.MoralScoreRecord;
import com.example.moral.entity.Role;
import com.example.moral.entity.Student;
import com.example.moral.entity.User;
import com.example.moral.repository.MoralScoreRecordRepository;
import com.example.moral.repository.StudentRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentRepository studentRepository;
    private final MoralScoreRecordRepository moralScoreRecordRepository;
    private final AuthService authService;

    public StudentController(StudentRepository studentRepository,
                             MoralScoreRecordRepository moralScoreRecordRepository,
                             AuthService authService) {
        this.studentRepository = studentRepository;
        this.moralScoreRecordRepository = moralScoreRecordRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<Student> getAllStudents(@RequestHeader HttpHeaders headers) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() == Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "学生无权限访问所有学生数据");
        }
        return studentRepository.findAll();
    }

    @GetMapping("/rankings")
    public ResponseEntity<Map<String, Object>> getClassRankings(@RequestHeader(name = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            SecurityUtils.requireUser(authService, authorization.substring(7));
        }

        List<Student> allStudents = studentRepository.findAll();
        String className = allStudents.stream()
                .findFirst()
                .map(student -> {
                    String grade = Optional.ofNullable(student.getGrade()).orElse("");
                    String classNameValue = Optional.ofNullable(student.getClassName()).orElse("");
                    return (grade + " " + classNameValue).trim();
                })
                .filter(name -> !name.isBlank())
                .orElse("未知班级");

        List<Map<String, Object>> studentRankings = allStudents.stream()
                .sorted(Comparator.comparingInt((Student student) -> Optional.ofNullable(student.getTotalScore()).orElse(0)).reversed())
                .map(student -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("studentNumber", student.getStudentNumber());
                    item.put("name", student.getName());
                    item.put("totalScore", Optional.ofNullable(student.getTotalScore()).orElse(0));
                    return item;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("className", className);
        result.put("rankings", studentRankings);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestHeader HttpHeaders headers,
                                                 @RequestBody Student student) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        student.setTotalScore(0);
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @GetMapping("/me")
    public ResponseEntity<Student> getMyStudentInfo(@RequestHeader HttpHeaders headers) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有学生可以查看个人信息");
        }
        return studentRepository.findByStudentNumber(user.getUsername())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "当前学生信息不存在"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(@RequestHeader HttpHeaders headers,
                                              @PathVariable Long id) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        assertStudentAccess(user, id, "学生只能查看自己的信息");
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/scores")
    public ResponseEntity<List<MoralScoreRecord>> getStudentScores(@RequestHeader HttpHeaders headers,
                                                                   @PathVariable Long id) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        assertStudentAccess(user, id, "学生只能查看自己的成绩");
        return studentRepository.findById(id)
                .map(student -> ResponseEntity.ok(moralScoreRecordRepository.findByStudent(student)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@RequestHeader HttpHeaders headers,
                                                 @PathVariable Long id,
                                                 @RequestBody Map<String, String> updates) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        SecurityUtils.requireRole(user, Role.ADMIN);
        return studentRepository.findById(id)
                .map(student -> {
                    if (updates.containsKey("name")) {
                        student.setName(updates.get("name"));
                    }
                    if (updates.containsKey("grade")) {
                        student.setGrade(updates.get("grade"));
                    }
                    if (updates.containsKey("className")) {
                        student.setClassName(updates.get("className"));
                    }
                    return ResponseEntity.ok(studentRepository.save(student));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void assertStudentAccess(User user, Long studentId, String errorMessage) {
        if (user.getRole() == Role.STUDENT) {
            Student self = studentRepository.findByStudentNumber(user.getUsername()).orElse(null);
            if (self == null || !self.getId().equals(studentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
            }
        }
    }
}
