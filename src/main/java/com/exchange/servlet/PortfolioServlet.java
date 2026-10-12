package com.exchange.servlet;

import com.exchange.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet exposing user balances, positions, and net worth
 * (Presentation Slide 10 / 12).
 */
public class PortfolioServlet extends BaseServlet {
    private final PortfolioService portfolioService;

    public PortfolioServlet(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String userId = authenticateUser(req);
            if (userId == null) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing user authentication");
                return;
            }

            Map<String, Object> summary = portfolioService.getPortfolioSummary(userId);
            sendJson(resp, HttpServletResponse.SC_OK, summary);
        } catch (IllegalArgumentException ex) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve portfolio: " + ex.getMessage());
        }
    }
}
