package com.exchange.dao;

import com.exchange.db.DatabaseManager;
import com.exchange.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User accounts and balances.
 * Separates database operations from business logic (Presentation Slide 09 / 12).
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final DatabaseManager dbManager;

    public UserDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, username, password_hash, full_name, balance, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setDouble(5, user.getBalance());
            return ps.executeUpdate() > 0;
        }
    }

    public User findById(String userId) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, full_name, balance, created_at FROM users WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, full_name, balance, created_at FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public List<User> findAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, full_name, balance, created_at FROM users ORDER BY username ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        }
        return list;
    }

    /**
     * Atomically adjusts user balance within an ongoing transaction.
     */
    public boolean adjustBalance(Connection conn, String userId, double delta) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE user_id = ? AND (balance + ?) >= 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setString(2, userId);
            ps.setDouble(3, delta);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    public boolean updateBalance(String userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        long epoch = rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").getTime() : System.currentTimeMillis();
        return new User(
                rs.getString("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getDouble("balance"),
                epoch
        );
    }
}
