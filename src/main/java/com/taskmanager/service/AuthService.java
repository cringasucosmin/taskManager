package com.taskmanager.service;

import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    // In-memory token store: token -> userId
    // Simple approach for a portfolio project (no JWT complexity needed)
    private final Map<String, Long> tokenStore = new HashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        userRepository.save(new User(username, password));
    }

    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        // Generate a simple token: base64(username:userId)
        User user = userOpt.get();
        String token = Base64.getEncoder().encodeToString(
            (username + ":" + user.getId() + ":" + System.currentTimeMillis()).getBytes()
        );
        tokenStore.put(token, user.getId());
        return token;
    }

    // Validates a token and returns the userId, or throws if invalid
    public Long validateToken(String token) {
        if (token == null || !tokenStore.containsKey(token)) {
            throw new IllegalArgumentException("Invalid or missing token");
        }
        return tokenStore.get(token);
    }

    // Removes token on logout
    public void logout(String token) {
        tokenStore.remove(token);
    }
}
