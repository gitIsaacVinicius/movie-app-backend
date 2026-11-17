package com.example.movie_app_api.controllers;

import com.example.movie_app_api.domain.user.User;
import com.example.movie_app_api.dto.ResponseDTO;
import com.example.movie_app_api.dto.UserResponseDTO;
import com.example.movie_app_api.infra.security.TokenService;
import com.example.movie_app_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserResponseDTO(user.getName(), user.getEmail()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal User user, @RequestBody UserResponseDTO body) {
        if (body.email() != null && !body.email().equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(body.email());

            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            user.setEmail(body.email());
        }

        if (body.name() != null && !body.name().isBlank()) {
            user.setName(body.name());
        }
        userRepository.save(user);

        String newToken = tokenService.generateToken(user);

        return ResponseEntity.ok(new UserResponseDTO(user.getName(), newToken));
    }
}
