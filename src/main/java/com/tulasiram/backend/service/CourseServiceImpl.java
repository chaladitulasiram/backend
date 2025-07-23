package com.tulasiram.backend.service;

import com.tulasiram.backend.dto.CourseRequest;
import com.tulasiram.backend.dto.SubmissionDto;
import com.tulasiram.backend.model.Assignment;
import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.CourseModule;
import com.tulasiram.backend.model.User;
import com.tulasiram.backend.repository.AssignmentRepository;
import com.tulasiram.backend.repository.CourseModuleRepository;
import com.tulasiram.backend.repository.CourseRepository;
import com.tulasiram.backend.repository.UserRepository;
import com.tulasiram.backend.validation.InputValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final InputValidator inputValidator;
    private final FileStorageService fileStorageService;
    private final CourseModuleRepository courseModuleRepository;
    private final AssignmentRepository assignmentRepository;

    @Override
    @Transactional
    public Course createCourse(CourseRequest courseRequest, Long mentorId) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new EntityNotFoundException("Mentor not found with id: " + mentorId));

        Course course = new Course();
        course.setName(courseRequest.getName());
        course.setDescription(courseRequest.getDescription());
        course.setCategory(courseRequest.getCategory());
        course.setDuration(courseRequest.getDuration());
        course.setMentor(mentor);

        return courseRepository.saveAndFlush(course);
    }

    @Override
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findByDeletedFalse(pageable);
    }

    @Override
    @Transactional
    public void enrollUserInCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        if (user.getEnrolledCourses().contains(course)) {
            throw new IllegalStateException("User is already enrolled in this course");
        }

        user.getEnrolledCourses().add(course);
        course.getEnrolledUsers().add(user);
        userRepository.save(user);
    }

    @Override
    public List<Course> getCoursesByMentorId(Long mentorId) {
        logger.info("Fetching courses for mentor ID: {}", mentorId);
        List<Course> courses = courseRepository.findByMentorIdAndDeletedFalse(mentorId);
        logger.info("Found {} courses for mentor ID: {}", courses.size(), mentorId);
        return courses;
    }

    @Override
    @Transactional
    public void addModuleToCourse(Long courseId, String moduleTitle, MultipartFile videoFile, int duration, User currentUser) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (!course.getMentor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the course mentor can add modules");
        }
        String videoUrl = fileStorageService.uploadVideoFile(videoFile);
        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(moduleTitle);
        module.setDuration(duration);
        module.setSessionVideoUrl(videoUrl);
        courseModuleRepository.save(module);
    }

    @Override
    @Transactional
    public void addAssignmentToCourse(Long courseId, String assignmentTitle, MultipartFile assignmentFile, User currentUser) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (!course.getMentor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the course mentor can add assignments");
        }
        String assignmentUrl = fileStorageService.uploadDocumentFile(assignmentFile);
        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(assignmentTitle);
        assignment.setAssignmentFileUrl(assignmentUrl);
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void uploadStudentAssignment(Long assignmentId, Long userId, MultipartFile answerFile) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!user.getEnrolledCourses().contains(assignment.getCourse())) {
            throw new AccessDeniedException("User is not enrolled in this course");
        }
        String answerFileUrl = fileStorageService.uploadDocumentFile(answerFile);
        assignment.setStudentAnswerFileUrl(answerFileUrl);
        assignment.setSubmittedByUserId(userId);
        assignmentRepository.save(assignment);
    }

    @Override
    public List<SubmissionDto> getSubmissionsForCourse(Long courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (!course.getMentor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view submissions for this course.");
        }
        List<Assignment> submittedAssignments = assignmentRepository.findByCourseIdAndStudentAnswerFileUrlNotNull(courseId);
        return submittedAssignments.stream().map(assignment -> {
            User student = userRepository.findById(assignment.getSubmittedByUserId()).orElse(null);
            return new SubmissionDto(
                    assignment.getId(),
                    assignment.getTitle(),
                    student != null ? student.getId() : null,
                    student != null ? student.getUsername() : "Unknown Student",
                    assignment.getStudentAnswerFileUrl(),
                    assignment.getGrade()
            );
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void gradeAssignment(Long assignmentId, int grade, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        if (!assignment.getCourse().getMentor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the course mentor can grade assignments");
        }
        assignment.setGrade(grade);
        assignmentRepository.save(assignment);
    }
}