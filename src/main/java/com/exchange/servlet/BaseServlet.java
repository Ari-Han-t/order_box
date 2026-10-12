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

        if (token != null && !token.isEmpty()) {
            String userId = SecurityUtil.getUserIdFromToken(token);
            if (userId != null) return userId;
        }

        // Check X-User-Id header or parameter (supports username like 'alice' or ID like 'u-alice-002')
        String xUser = req.getHeader("X-User-Id");
        if (xUser == null || xUser.trim().isEmpty()) {
            xUser = req.getParameter("userId");
        }
        if (xUser != null && !xUser.trim().isEmpty()) {
            String val = xUser.trim();
            if (val.startsWith("u-")) return val;
            if ("alice".equalsIgnoreCase(val)) return "u-alice-002";
            if ("bob".equalsIgnoreCase(val)) return "u-bob-003";
            if ("charlie".equalsIgnoreCase(val)) return "u-charlie-004";
            if ("marketmaker".equalsIgnoreCase(val) || "market_maker".equalsIgnoreCase(val)) return "u-marketmaker-001";
            if ("demo_trader".equalsIgnoreCase(val) || "demo".equalsIgnoreCase(val)) return "u-demo-005";
            return val;
        }

        return null;
    }
}
