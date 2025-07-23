package com.tulasiram.backend.repository;

import com.tulasiram.backend.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    // We can add custom query methods here later if needed
    List<Assignment> findByCourseIdAndStudentAnswerFileUrlNotNull(Long courseId);

}