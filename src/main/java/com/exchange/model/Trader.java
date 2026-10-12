package com.exchange.model;

/**
 * Concrete domain entity representing an active market participant (Trader).
 * Inherits from {@link User} to demonstrate OOP inheritance.
 * Extends user behavior with portfolio awareness and order submission utilities.
 */
public class Trader extends User {
    private static final long serialVersionUID = 1L;

    public Trader(String userId, String username, String passwordHash, String fullName, double balance) {
        super(userId, username, passwordHash, fullName, balance);
    }

    public Trader(String userId, String username, String passwordHash, String fullName,
                  double balance, long createdAtEpoch) {
        super(userId, username, passwordHash, fullName, balance, createdAtEpoch);
    }

    /**
     * Factory method to create a Trader domain model from a base User entity.
     */
    public static Trader fromUser(User user) {
        if (user == null) return null;
        if (user instanceof Trader trader) return trader;
        return new Trader(user.getUserId(), user.getUsername(), user.getPasswordHash(),
                user.getFullName(), user.getBalance(), user.getCreatedAtEpoch());
    }
}
