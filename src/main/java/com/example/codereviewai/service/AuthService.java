package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.LoginRequest;
import com.example.codereviewai.dto.request.RegisterRequest;
import com.example.codereviewai.dto.response.AuthResponse;
import com.example.codereviewai.entity.User;
import com.example.codereviewai.exception.DuplicateResourceException;
import com.example.codereviewai.repository.UserRepository;
import com.example.codereviewai.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());
        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername(), request.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("User logged in successfully: {}", request.getUsername());
        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}