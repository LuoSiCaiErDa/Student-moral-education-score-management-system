package com.example.moral.service;

import com.example.moral.entity.Role;
import com.example.moral.entity.User;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.Set;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class SecurityUtils {
    public static String resolveToken(HttpHeaders headers) {
        String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    public static User requireUser(AuthService authService, String token) {
        return authService.getUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "令牌无效或缺失"));
    }

    public static void requireRole(User user, Role required) {
        if (user.getRole() != required) {
            throw new ResponseStatusException(FORBIDDEN, "权限不足");
        }
    }

    public static void requireAnyRole(User user, Role... requiredRoles) {
        Set<Role> roles = EnumSet.of(requiredRoles[0], requiredRoles);
        if (!roles.contains(user.getRole())) {
            throw new ResponseStatusException(FORBIDDEN, "Insufficient role");
        }
    }
}
