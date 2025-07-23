package com.tulasiram.backend.service;

import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    List<Course> getEnrolledCoursesByUserId(Long userId);

    List<User> getAllUsers();

    Page<User> getAllUsers(Pageable pageable);

    User getUserById(Long id);

    User updateUser(Long id, User updatedUser, User currentUser);

    User registerUser(User user);

    void deleteUser(Long id, User currentUser);

    Optional<User> findByUsername(String username);

    Optional<User> validateUser(String username, String password);

    boolean isPasswordValid(User user, String currentPassword);

    void updatePassword(User user, String newPassword);



    long countByRole(String role);


}
