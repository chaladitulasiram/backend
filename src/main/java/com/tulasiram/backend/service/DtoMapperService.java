package com.tulasiram.backend.service;

import com.tulasiram.backend.dto.AssignmentDto;
import com.tulasiram.backend.dto.CourseDto;
import com.tulasiram.backend.dto.CourseModuleDto;
import com.tulasiram.backend.dto.UserDto;
import com.tulasiram.backend.model.Assignment;
import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.CourseModule;
import com.tulasiram.backend.model.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class DtoMapperService {

    // --- USER MAPPING ---
    public UserDto toUserDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        return dto;
    }

    // --- COURSE MAPPING (UPDATED) ---
    public CourseDto toCourseDto(Course course) {
        if (course == null) return null;
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setCategory(course.getCategory());
        dto.setDuration(course.getDuration());
        dto.setMentor(toUserDto(course.getMentor()));

        if (course.getModules() != null) {
            dto.setModules(course.getModules().stream().map(this::toCourseModuleDto).collect(Collectors.toList()));
        }
        if (course.getAssignments() != null) {
            dto.setAssignments(course.getAssignments().stream().map(this::toAssignmentDto).collect(Collectors.toList()));
        }

        return dto;
    }

    // --- HELPER MAPPING METHODS ---
    public CourseModuleDto toCourseModuleDto(CourseModule module) {
        if (module == null) return null;
        CourseModuleDto dto = new CourseModuleDto();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        // Add other module fields here if needed in the future
        return dto;
    }

    public AssignmentDto toAssignmentDto(Assignment assignment) {
        if (assignment == null) return null;
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setTitle(assignment.getTitle());
        // Add other assignment fields here if needed in the future
        return dto;
    }
}