package com.tulasiram.backend.dto;
import lombok.Data;
import java.util.List;
@Data
public class CourseDto {
    private Long id;
    private String name;
    private String description;
    private int duration;
    private String category;
    private UserDto mentor;
    private List<CourseModuleDto> modules;
    private List<AssignmentDto> assignments;
}