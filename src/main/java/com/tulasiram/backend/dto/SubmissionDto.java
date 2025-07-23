package com.tulasiram.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentUsername;
    private String answerFileUrl;
    private Integer grade;
}