package com.gradproject.controller;

import com.gradproject.dto.RegisterDTO;
import com.gradproject.entity.User;
import com.gradproject.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContextHolder.getContext().setAuthentication(auth);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        try {
            User user = userService.register(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        response.put("studentId", user.getStudentId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchStudents(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchStudents(query).stream()
                .filter(u -> u.getRole().name().equals("STUDENT"))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("name", u.getName());
                    m.put("email", u.getEmail());
                    m.put("studentId", u.getStudentId());
                    return m;
                }).toList());
    }
}
