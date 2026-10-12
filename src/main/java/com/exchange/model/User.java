package com.exchange.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base User entity representing an authenticated account holder in the system.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String username;
    private String passwordHash;
    private String fullName;
    private double balance;
    private final long createdAtEpoch;

    public User(String userId, String username, String passwordHash, String fullName, double balance) {
        this(userId, username, passwordHash, fullName, balance, System.currentTimeMillis());
    }

    @JsonCreator
    public User(@JsonProperty("userId") String userId,
                @JsonProperty("username") String username,
                @JsonProperty("passwordHash") String passwordHash,
                @JsonProperty("fullName") String fullName,
                @JsonProperty("balance") double balance,
                @JsonProperty("createdAtEpoch") long createdAtEpoch) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (balance < 0.0) {
            throw new IllegalArgumentException("Initial balance cannot be negative: " + balance);
        }

        this.userId = userId;
        this.username = username.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.fullName = fullName != null ? fullName.trim() : username;
        this.balance = balance;
        this.createdAtEpoch = createdAtEpoch > 0 ? createdAtEpoch : System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized void setBalance(double balance) {
        if (balance < 0.0) {
            throw new IllegalArgumentException("Account balance cannot be negative: " + balance);
        }
        this.balance = balance;
    }

    public synchronized boolean hasSufficientBalance(double amount) {
        return this.balance >= amount;
    }

    public synchronized void debitBalance(double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException("Debit amount cannot be negative: " + amount);
        }
        if (this.balance < amount) {
            throw new IllegalStateException(String.format(
                    "Insufficient funds: available $%.2f, required $%.2f", this.balance, amount));
        }
        this.balance -= amount;
    }

    public synchronized void creditBalance(double amount) {
        if (amount < 0.0) {
            throw new IllegalArgumentException("Credit amount cannot be negative: " + amount);
        }
        this.balance += amount;
    }

    public long getCreatedAtEpoch() {
        return createdAtEpoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, user=%s, balance=$%.2f]", userId, username, balance);
    }
}
