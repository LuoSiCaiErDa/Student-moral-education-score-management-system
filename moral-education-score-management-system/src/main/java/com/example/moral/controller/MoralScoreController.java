package com.example.moral.controller;

import com.example.moral.entity.MoralScoreRecord;
import com.example.moral.entity.RecordStatus;
import com.example.moral.entity.Role;
import com.example.moral.entity.Student;
import com.example.moral.entity.User;
import com.example.moral.repository.MoralScoreRecordRepository;
import com.example.moral.repository.StudentRepository;
import com.example.moral.service.AuthService;
import com.example.moral.service.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class MoralScoreController {
    private final MoralScoreRecordRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;

    public MoralScoreController(MoralScoreRecordRepository scoreRepository,
                                StudentRepository studentRepository,
                                AuthService authService) {
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<MoralScoreRecord> getAllScores(@RequestHeader HttpHeaders headers) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() == Role.STUDENT) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "学生无权限访问所有记录");
        }
        return scoreRepository.findAll();
    }

    @GetMapping("/pending")
    public List<MoralScoreRecord> getPendingScores(@RequestHeader HttpHeaders headers) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.TEACHER) {
            return scoreRepository.findAll().stream()
                    .filter(record -> record.getStatus() == RecordStatus.PENDING)
                    .toList();
        }
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "权限不足");
    }

    @PostMapping
    public ResponseEntity<?> addScore(@RequestHeader HttpHeaders headers,
                                      @RequestBody Map<String, String> payload) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN && user.getRole() != Role.CLASS_CADRE) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "只有管理员、教师或班干部可以提交德育分记录");
        }
        String studentNumber = payload.get("studentNumber");
        if (studentNumber == null) {
            return ResponseEntity.badRequest().body("studentNumber is required");
        }
        Student student = studentRepository.findByStudentNumber(studentNumber).orElse(null);
        if (student == null) {
            return ResponseEntity.badRequest().body("Student not found");
        }

        MoralScoreRecord record = new MoralScoreRecord();
        record.setStudent(student);
        record.setCategory(payload.getOrDefault("category", "一般行为"));
        record.setScore(Integer.parseInt(payload.getOrDefault("score", "0")));
        record.setTerm(payload.getOrDefault("term", "未知学期"));
        record.setTeacherName(user.getDisplayName() == null ? user.getUsername() : user.getDisplayName());
        record.setRemark(payload.getOrDefault("remark", ""));
        record.setStatus(RecordStatus.PENDING);
        record.setSubmittedAt(LocalDateTime.now());

        MoralScoreRecord saved = scoreRepository.save(record);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/audit")
    public ResponseEntity<MoralScoreRecord> auditScore(@RequestHeader HttpHeaders headers,
                                                        @PathVariable Long id,
                                                        @RequestBody Map<String, String> payload) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.TEACHER) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "只有管理员或教师可以审核德育分记录");
        }
        return scoreRepository.findById(id)
                .map(record -> {
                    String action = payload.getOrDefault("action", "REJECTED");
                    RecordStatus status = "APPROVED".equalsIgnoreCase(action) ? RecordStatus.APPROVED : RecordStatus.REJECTED;
                    record.setStatus(status);
                    record.setAuditComment(payload.getOrDefault("comment", ""));
                    record.setAuditor(user.getDisplayName() == null ? user.getUsername() : user.getDisplayName());
                    record.setAuditedAt(LocalDateTime.now());
                    if (status == RecordStatus.APPROVED) {
                        Student student = record.getStudent();
                        if (student != null && record.getScore() != null) {
                            student.setTotalScore(student.getTotalScore() + record.getScore());
                            studentRepository.save(student);
                        }
                    }
                    return ResponseEntity.ok(scoreRepository.save(record));
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScore(@RequestHeader HttpHeaders headers,
                                       @PathVariable Long id) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() != Role.ADMIN) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "只有管理员可以删除记录");
        }
        return scoreRepository.findById(id)
                .map(record -> {
                    scoreRepository.delete(record);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
