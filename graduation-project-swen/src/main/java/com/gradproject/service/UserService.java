package com.gradproject.service;

import com.gradproject.dto.RegisterDTO;
import com.gradproject.entity.Role;
import com.gradproject.entity.User;
import com.gradproject.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String pwd = dto.getPassword();
        if (pwd.length() < 8 || !pwd.matches(".*[A-Z].*") || !pwd.matches(".*[0-9].*")) {
            throw new RuntimeException("Password must be at least 8 characters with one uppercase letter and one number");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        user.setStudentId(dto.getStudentId());

        User saved = userRepository.save(user);
        logger.info("AUDIT - User registered: id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> searchStudents(String query) {
        return userRepository.findByEmailContainingOrStudentIdContaining(query, query);
    }
}
