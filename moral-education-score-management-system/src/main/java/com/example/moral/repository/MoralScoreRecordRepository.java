package com.example.moral.repository;

import com.example.moral.entity.MoralScoreRecord;
import com.example.moral.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MoralScoreRecordRepository extends JpaRepository<MoralScoreRecord, Long> {
    List<MoralScoreRecord> findByStudent(Student student);
}
