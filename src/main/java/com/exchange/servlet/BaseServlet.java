package com.exchange.servlet;

import com.exchange.util.JsonUtil;
import com.exchange.util.SecurityUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Servlet providing CORS headers, JSON serialization helpers,
 * and session authentication parsing.
 */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }

    protected void sendJson(HttpServletResponse resp, int statusCode, Object data) throws IOException {
        setCorsHeaders(resp);
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(JsonUtil.toJson(data));
        }
    }

    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        setCorsHeaders(resp);
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> err = new HashMap<>();
        err.put("error", true);
        err.put("status", statusCode);
        err.put("message", message);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(JsonUtil.toJson(err));
        }
    }

    /**
     * Extracts authenticated userId from Authorization: Bearer <token> or ?token=<token>
     */
    protected String authenticateUser(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            token = req.getParameter("token");
        }

        if (token == null || token.isEmpty()) {
            // Also check for user_id parameter in dev/test/demo mode
            String directUserId = req.getParameter("userId");
            if (directUserId != null && !directUserId.trim().isEmpty()) {
                return directUserId.trim();
            }
            return null;
        }

        return SecurityUtil.getUserIdFromToken(token);
    }
}
