package com.exchange.servlet;

import com.exchange.model.User;
import com.exchange.service.AuthService;
import com.exchange.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Servlet managing user registration, login, and identity verification.
 */
public class AuthServlet extends BaseServlet {
    private final AuthService authService;

    public AuthServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try {
            JsonNode body = JsonUtil.fromJson(req.getInputStream(), JsonNode.class);

            if (pathInfo.endsWith("/register")) {
                String username = body.has("username") ? body.get("username").asText() : null;
                String password = body.has("password") ? body.get("password").asText() : null;
                String fullName = body.has("fullName") ? body.get("fullName").asText() : username;
                double initialBalance = body.has("initialBalance") ? body.get("initialBalance").asDouble() : 10000.0;

                Map<String, Object> result = authService.register(username, password, fullName, initialBalance);
                sendJson(resp, HttpServletResponse.SC_CREATED, result);
            } else {
                // Default to login
                String username = body.has("username") ? body.get("username").asText() : null;
                String password = body.has("password") ? body.get("password").asText() : null;

                Map<String, Object> result = authService.login(username, password);
                sendJson(resp, HttpServletResponse.SC_OK, result);
            }
        } catch (IllegalArgumentException ex) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + ex.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try {
            if (pathInfo.endsWith("/users")) {
                List<User> users = authService.getAllUsers();
                sendJson(resp, HttpServletResponse.SC_OK, users);
                return;
            }

            String userId = authenticateUser(req);
            if (userId == null) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing or invalid session token");
                return;
            }

            User user = authService.getUserById(userId);
            if (user == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            sendJson(resp, HttpServletResponse.SC_OK, user);
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + ex.getMessage());
        }
    }
}
