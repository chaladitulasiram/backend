package com.tulasiram.backend.repository;

import com.tulasiram.backend.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByDeletedFalse(Pageable pageable);

    Optional<Course> findByIdAndDeletedFalse(Long id);

    // THIS IS THE FIX: We are reverting to the standard JPA method name.
    // This is more reliable than a custom query.
    List<Course> findByMentorIdAndDeletedFalse(Long mentorId);
}