package com.tulasiram.backend.service;

import com.tulasiram.backend.model.Course;
import com.tulasiram.backend.model.User;
import com.tulasiram.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // Note: We will add validation and audit services later.
    // private final InputValidator inputValidator;
    // private final AuditService auditService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        if (userRepository.findByUsernameAndDeletedFalse(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already exists.");
        }
        if (userRepository.findByEmailAndDeletedFalse(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findByDeletedFalse(pageable);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id, User currentUser) {
        User user = getUserById(id);
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        // user.setDeletedBy(currentUser.getId()); // Uncomment when currentUser logic is added
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User updatedUser, User currentUser) {
        User user = getUserById(id);
        if (updatedUser.getUsername() != null) {
            user.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getRole() != null) {
            user.setRole(updatedUser.getRole());
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean isPasswordValid(User user, String currentPassword) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username);
    }

    @Override
    public long countByRole(String role) {
        return userRepository.countByRoleAndDeletedFalse(role);
    }

    @Override
    public List<Course> getEnrolledCoursesByUserId(Long userId) {
        User user = getUserById(userId);
        return new ArrayList<>(user.getEnrolledCourses());
    }

    @Override
    public Optional<User> validateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsernameAndDeletedFalse(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

}