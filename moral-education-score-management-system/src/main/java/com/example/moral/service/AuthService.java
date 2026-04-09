package com.example.moral.service;

import com.example.moral.entity.User;
import com.example.moral.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password));
    }

    public String createToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user);
        return token;
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenStore.get(token));
    }

    public void invalidate(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }
}
