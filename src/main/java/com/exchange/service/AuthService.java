package com.exchange.service;

import com.exchange.dao.UserDAO;
import com.exchange.model.User;
import com.exchange.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service handling Trader authentication, registration, and session tokens.
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Map<String, Object> register(String username, String password, String fullName, double initialBalance) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters");
        }
        if (initialBalance < 0.0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        User existing = userDAO.findByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String hash = SecurityUtil.hashPassword(password);
        User newUser = new User(userId, username, hash, fullName, initialBalance);
        userDAO.createUser(newUser);

        String token = SecurityUtil.generateSessionToken(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("user", newUser);
        return res;
    }

    public Map<String, Object> login(String username, String password) throws SQLException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        User user = userDAO.findByUsername(username);
        if (user == null || !SecurityUtil.checkPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = SecurityUtil.generateSessionToken(user.getUserId());
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("user", user);
        return res;
    }

    public User getUserById(String userId) throws SQLException {
        if (userId == null) return null;
        return userDAO.findById(userId);
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAllUsers();
    }
}
