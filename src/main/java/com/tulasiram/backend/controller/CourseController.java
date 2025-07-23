package com.tulasiram.backend.controller;

import com.tulasiram.backend.dto.CourseDto;
import com.tulasiram.backend.dto.CourseRequest;
import com.tulasiram.backend.dto.SubmissionDto;
import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.User;
import com.tulasiram.backend.service.CourseService;
import com.tulasiram.backend.service.DtoMapperService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final DtoMapperService dtoMapperService;

    @PostMapping
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseRequest courseRequest,
            @AuthenticationPrincipal User mentor) {

        try {
            // THIS IS THE FIX: We pass the mentor's ID instead of the whole object.
            Course newCourse = courseService.createCourse(courseRequest, mentor.getId());
            CourseDto courseDto = dtoMapperService.toCourseDto(newCourse);
            return ResponseEntity.status(HttpStatus.CREATED).body(courseDto);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Page<CourseDto>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> coursePage = courseService.getAllCourses(pageable);

        Page<CourseDto> courseDtoPage = coursePage.map(dtoMapperService::toCourseDto);
        return ResponseEntity.ok(courseDtoPage);
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> enrollInCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User user) {
        try {
            courseService.enrollUserInCourse(user.getId(), courseId);
            return ResponseEntity.ok("Successfully enrolled in course.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-creations")
    @PreAuthorize("hasAuthority('ROLE_MENTOR')")
    public ResponseEntity<List<CourseDto>> getMyCreatedCourses(@AuthenticationPrincipal User mentor) {
        List<Course> courses = courseService.getCoursesByMentorId(mentor.getId());
        List<CourseDto> courseDtos = courses.stream()
                .map(dtoMapperService::toCourseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDtos);
    }

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasAuthority('ROLE_MENTOR')")
    public ResponseEntity<?> addModule(
            @PathVariable Long courseId,
            @RequestParam("title") String title,
            @RequestParam("duration") int duration,
            @RequestParam("video") MultipartFile videoFile,
            @AuthenticationPrincipal User mentor) {

        try {
            courseService.addModuleToCourse(courseId, title, videoFile, duration, mentor);
            return ResponseEntity.ok("Module added successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload video: " + e.getMessage());
        }

    }
    @PostMapping("/assignments/{assignmentId}/submit") // ADD THIS ENTIRE METHOD
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile answerFile,
            @AuthenticationPrincipal User user) {

        try {
            courseService.uploadStudentAssignment(assignmentId, user.getId(), answerFile);
            return ResponseEntity.ok("Assignment submitted successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/{courseId}/submissions") // ADD THIS
    @PreAuthorize("hasAuthority('ROLE_MENTOR')")
    public ResponseEntity<List<SubmissionDto>> getSubmissions(@PathVariable Long courseId, @AuthenticationPrincipal User currentUser) {
        List<SubmissionDto> submissions = courseService.getSubmissionsForCourse(courseId, currentUser);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/assignments/{assignmentId}/grade") // ADD THIS
    @PreAuthorize("hasAuthority('ROLE_MENTOR')")
    public ResponseEntity<?> gradeAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("grade") int grade,
            @AuthenticationPrincipal User mentor) {
        try {
            courseService.gradeAssignment(assignmentId, grade, mentor);
            return ResponseEntity.ok("Grade submitted successfully.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
