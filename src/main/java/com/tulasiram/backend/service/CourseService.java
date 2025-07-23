package com.tulasiram.backend.service;

import com.tulasiram.backend.dto.CourseRequest;
import com.tulasiram.backend.dto.SubmissionDto;
import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService {

    Course createCourse(CourseRequest courseRequest, Long mentorId);

    Page<Course> getAllCourses(Pageable pageable);

    void enrollUserInCourse(Long userId, Long courseId);

    List<Course> getCoursesByMentorId(Long mentorId);

    void addModuleToCourse(Long courseId, String moduleTitle, MultipartFile videoFile, int duration, User currentUser) throws IOException;

    // THIS IS THE FIX: The missing method signature is now added.
    void addAssignmentToCourse(Long courseId, String assignmentTitle, MultipartFile assignmentFile, User currentUser) throws IOException;

    void uploadStudentAssignment(Long assignmentId, Long userId, MultipartFile answerFile) throws IOException; // ADD THIS LINE

    List<SubmissionDto> getSubmissionsForCourse(Long courseId, User currentUser); // ADD THIS LINE
    void gradeAssignment(Long assignmentId, int grade, User currentUser);









}