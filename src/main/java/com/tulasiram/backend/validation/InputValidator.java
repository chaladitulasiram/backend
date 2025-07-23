package com.tulasiram.backend.validation;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InputValidator {

    // We will add more validation methods (email, password, etc.) here later.
    // For now, let's add the ones for courses.

    public void validateCourseTitle(String title) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Course title cannot be empty");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Course title too long (max 200 characters)");
        }
    }

    public void validateCourseDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Course description cannot be empty");
        }
        if (description.length() > 2000) {
            throw new IllegalArgumentException("Course description too long (max 2000 characters)");
        }
    }
}