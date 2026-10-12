package com.exchange.servlet;

import com.exchange.db.DatabaseManager;
import com.exchange.engine.MatchingEngine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Health and Diagnostics Servlet.
 */
public class HealthServlet extends BaseServlet {
    private final DatabaseManager dbManager;
    private final MatchingEngine matchingEngine;

    public HealthServlet(DatabaseManager dbManager, MatchingEngine matchingEngine) {
        this.dbManager = dbManager;
        this.matchingEngine = matchingEngine;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Real-Time Electronic Stock Exchange");
        status.put("databaseMode", dbManager.isFallbackH2() ? "H2_EMBEDDED_MYSQL_MODE" : "MYSQL_PRIMARY");
        status.put("activeSymbols", matchingEngine.getActiveSymbols());
        status.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());
        status.put("timestamp", System.currentTimeMillis());

        sendJson(resp, HttpServletResponse.SC_OK, status);
    }
}
