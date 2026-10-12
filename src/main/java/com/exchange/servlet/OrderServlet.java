package com.exchange.servlet;

import com.exchange.model.*;
import com.exchange.service.TradingService;
import com.exchange.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling Order Placement (POST), Order Querying (GET),
 * and Order Cancellation (DELETE or POST /cancel) (Presentation Slide 10 / 12).
 */
public class OrderServlet extends BaseServlet {
    private final TradingService tradingService;

    public OrderServlet(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.contains("/cancel")) {
            handleCancel(req, resp);
            return;
        }

        try {
            String userId = authenticateUser(req);
            JsonNode body = JsonUtil.fromJson(req.getInputStream(), JsonNode.class);

            // Allow userId from body if not in auth header
            if (userId == null && body.has("userId")) {
                userId = body.get("userId").asText();
            }

            if (userId == null || userId.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing user authentication");
                return;
            }

            if (!body.has("symbol") || !body.has("side") || !body.has("quantity")) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Required fields: symbol, side, quantity");
                return;
            }

            String symbol = body.get("symbol").asText();
            OrderSide side = OrderSide.valueOf(body.get("side").asText().toUpperCase());
            OrderType type = body.has("type") ? OrderType.valueOf(body.get("type").asText().toUpperCase()) : OrderType.LIMIT;
            double price = body.has("price") ? body.get("price").asDouble() : 0.0;
            int quantity = body.get("quantity").asInt();

            MatchResult matchResult = tradingService.submitOrder(userId, symbol, side, type, price, quantity);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("order", matchResult.getIncomingOrder());
            responseData.put("tradesCount", matchResult.getTrades().size());
            responseData.put("trades", matchResult.getTrades());
            responseData.put("fullyFilled", matchResult.isFullyFilled());
            responseData.put("totalSharesExecuted", matchResult.getTotalSharesExecuted());
            responseData.put("totalExecutedAmount", matchResult.getTotalExecutedAmount());

            sendJson(resp, HttpServletResponse.SC_CREATED, responseData);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to submit order: " + ex.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String userId = authenticateUser(req);
            if (userId == null) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: missing user authentication");
                return;
            }

            boolean activeOnly = "true".equalsIgnoreCase(req.getParameter("activeOnly"));
            int limit = req.getParameter("limit") != null ? Integer.parseInt(req.getParameter("limit")) : 50;

            List<Order> orders;
            if (activeOnly) {
                orders = tradingService.getActiveOrdersForUser(userId);
            } else {
                orders = tradingService.getOrderHistoryForUser(userId, limit);
            }

            sendJson(resp, HttpServletResponse.SC_OK, orders);
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch orders: " + ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleCancel(req, resp);
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String userId = authenticateUser(req);
            String pathInfo = req.getPathInfo();
            String orderId = null;

            if (pathInfo != null) {
                String clean = pathInfo.replaceFirst("^/", "").replace("/cancel", "");
                if (!clean.isEmpty()) {
                    orderId = clean;
                }
            }

            if (orderId == null) {
                orderId = req.getParameter("orderId");
            }

            if (orderId == null || orderId.trim().isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing orderId to cancel");
                return;
            }

            // Fallback userId if not authenticated
            if (userId == null) {
                userId = req.getParameter("userId");
            }

            if (userId == null) {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized to cancel order");
                return;
            }

            Order cancelled = tradingService.cancelOrder(userId, orderId);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Order successfully cancelled");
            res.put("cancelledOrder", cancelled);

            sendJson(resp, HttpServletResponse.SC_OK, res);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Order cancellation failed: " + ex.getMessage());
        }
    }
}
