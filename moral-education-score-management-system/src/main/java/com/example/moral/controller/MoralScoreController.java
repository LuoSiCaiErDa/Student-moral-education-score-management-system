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
import com.example.moral.util.ResponseUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "学生无权限访问所有记录");
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
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
    }

    @PostMapping
    public ResponseEntity<?> addScore(@RequestHeader HttpHeaders headers,
                                      @RequestBody Map<String, String> payload) {
        User user = SecurityUtils.requireUser(authService, SecurityUtils.resolveToken(headers));
        if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN && user.getRole() != Role.CLASS_CADRE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有管理员、教师或班干部可以提交德育分记录");
        }
        String studentNumber = payload.get("studentNumber");
        if (studentNumber == null || studentNumber.isBlank()) {
            return ResponseUtils.badRequest("studentNumber is required");
        }
        Student student = studentRepository.findByStudentNumber(studentNumber.trim()).orElse(null);
        if (student == null) {
            return ResponseUtils.badRequest("Student not found");
        }

        int score;
        try {
            score = Integer.parseInt(payload.getOrDefault("score", "0").trim());
        } catch (NumberFormatException e) {
            return ResponseUtils.badRequest("score 必须是整数");
        }

        MoralScoreRecord record = new MoralScoreRecord();
        record.setStudent(student);
        record.setCategory(payload.getOrDefault("category", "一般行为"));
        record.setScore(score);
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有管理员或教师可以审核德育分记录");
        }
        return scoreRepository.findById(id)
                .map(record -> {
                    String action = payload.getOrDefault("action", "REJECTED");
                    RecordStatus targetStatus = "APPROVED".equalsIgnoreCase(action) ? RecordStatus.APPROVED : RecordStatus.REJECTED;
                    RecordStatus currentStatus = record.getStatus();
                    record.setStatus(targetStatus);
                    record.setAuditComment(payload.getOrDefault("comment", ""));
                    record.setAuditor(user.getDisplayName() == null ? user.getUsername() : user.getDisplayName());
                    record.setAuditedAt(LocalDateTime.now());

                    Student student = record.getStudent();
                    if (student != null && record.getScore() != null) {
                        if (currentStatus != RecordStatus.APPROVED && targetStatus == RecordStatus.APPROVED) {
                            student.setTotalScore(Optional.ofNullable(student.getTotalScore()).orElse(0) + record.getScore());
                            studentRepository.save(student);
                        } else if (currentStatus == RecordStatus.APPROVED && targetStatus != RecordStatus.APPROVED) {
                            student.setTotalScore(Optional.ofNullable(student.getTotalScore()).orElse(0) - record.getScore());
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有管理员可以删除记录");
        }
        return scoreRepository.findById(id)
                .map(record -> {
                    scoreRepository.delete(record);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
