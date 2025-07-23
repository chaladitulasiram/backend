package com.tulasiram.backend.controller;

import com.tulasiram.backend.dto.CourseDto;
import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.User;
import com.tulasiram.backend.service.DtoMapperService;
import com.tulasiram.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DtoMapperService dtoMapperService;

    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseDto>> getMyEnrolledCourses(@AuthenticationPrincipal User currentUser) {
        // 1. Get the enrolled courses for the authenticated user
        List<Course> enrolledCourses = userService.getEnrolledCoursesByUserId(currentUser.getId());

        // 2. Convert the list of Course entities to a list of CourseDto
        List<CourseDto> courseDtos = enrolledCourses.stream()
                .map(dtoMapperService::toCourseDto)
                .collect(Collectors.toList());

        // 3. Return the list of DTOs
        return ResponseEntity.ok(courseDtos);
    }
}