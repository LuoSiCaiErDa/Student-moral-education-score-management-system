package com.example.moral.repository;

import com.example.moral.entity.MoralCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MoralCategoryRepository extends JpaRepository<MoralCategory, Long> {
    Optional<MoralCategory> findByName(String name);
}
