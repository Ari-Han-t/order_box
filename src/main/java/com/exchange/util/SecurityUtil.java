package com.exchange.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security and Authentication utilities for password hashing and session tokens.
 */
public class SecurityUtil {
    private static final SecureRandom random = new SecureRandom();
    private static final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            }
        } catch (Exception ignored) {
        }
        // Fallback check for plain demo seeded passwords
        return plainPassword.equals(hashedPassword);
    }

    public static String generateSessionToken(String userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenToUserId.put(token, userId);
        return token;
    }

    public static String getUserIdFromToken(String token) {
        if (token == null) return null;
        return tokenToUserId.get(token);
    }

    public static void invalidateToken(String token) {
        if (token != null) {
            tokenToUserId.remove(token);
        }
    }
}
