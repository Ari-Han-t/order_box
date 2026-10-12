package com.exchange.client;

import com.exchange.model.*;
import com.exchange.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Robust HTTP client communicating with the Exchange Jakarta Servlet REST API.
 * Uses native Java 21 HttpClient with connection timeouts and error handling.
 */
public class ExchangeApiClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private String authToken;
    private String currentUserId;
    private String currentUsername;

    public ExchangeApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    public synchronized void setSession(String token, String userId, String username) {
        this.authToken = token;
        this.currentUserId = userId;
        this.currentUsername = username;
    }

    public synchronized String getAuthToken() {
        return authToken;
    }

    public synchronized String getCurrentUserId() {
        return currentUserId;
    }

    public synchronized String getCurrentUsername() {
        return currentUsername;
    }

    public synchronized boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    public Map<String, Object> login(String username, String password) throws Exception {
        Map<String, String> payload = Map.of("username", username, "password", password);
        String body = JsonUtil.toJson(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException(extractErrorMessage(resp.body()));
        }

        JsonNode root = JsonUtil.fromJson(resp.body(), JsonNode.class);
        String token = root.get("token").asText();
        JsonNode userNode = root.get("user");
        String uid = userNode.get("userId").asText();
        String uName = userNode.get("username").asText();

        setSession(token, uid, uName);

        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("userId", uid);
        res.put("username", uName);
        res.put("balance", userNode.get("balance").asDouble());
        return res;
    }

    public Map<String, Object> register(String username, String password, String fullName, double initialBalance) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("fullName", fullName);
        payload.put("initialBalance", initialBalance);
        String body = JsonUtil.toJson(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new RuntimeException(extractErrorMessage(resp.body()));
        }

        JsonNode root = JsonUtil.fromJson(resp.body(), JsonNode.class);
        String token = root.get("token").asText();
        JsonNode userNode = root.get("user");
        String uid = userNode.get("userId").asText();
        String uName = userNode.get("username").asText();

        setSession(token, uid, uName);
        return Map.of("token", token, "userId", uid, "username", uName);
    }

    public List<User> getAllUsers() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/users"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<List<User>>() {});
        }
        return Collections.emptyList();
    }

    public List<Stock> getStocks() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/stocks"))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<List<Stock>>() {});
        }
        return Collections.emptyList();
    }

    public OrderBookSnapshot getOrderBook(String symbol, int depth) throws Exception {
        String uri = String.format("%s/api/orderbook?symbol=%s&depth=%d", baseUrl, symbol, depth);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.fromJson(resp.body(), OrderBookSnapshot.class);
        }
        throw new RuntimeException(extractErrorMessage(resp.body()));
    }

    public List<Trade> getRecentTrades(String symbol, int limit) throws Exception {
        String uri = String.format("%s/api/trades?symbol=%s&limit=%d", baseUrl, symbol, limit);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<List<Trade>>() {});
        }
        return Collections.emptyList();
    }

    public Map<String, Object> getPortfolio() throws Exception {
        ensureAuthenticated();
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/portfolio"))
                .timeout(Duration.ofSeconds(4))
                .GET();
        applyAuth(b);

        HttpResponse<String> resp = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        }
        throw new RuntimeException(extractErrorMessage(resp.body()));
    }

    public List<Order> getActiveOrders() throws Exception {
        ensureAuthenticated();
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orders?activeOnly=true"))
                .timeout(Duration.ofSeconds(4))
                .GET();
        applyAuth(b);

        HttpResponse<String> resp = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return parseOrdersList(resp.body());
        }
        return Collections.emptyList();
    }

    public Map<String, Object> submitOrder(String symbol, OrderSide side, OrderType type, double price, int quantity) throws Exception {
        ensureAuthenticated();
        Map<String, Object> payload = new HashMap<>();
        payload.put("symbol", symbol);
        payload.put("side", side.name());
        payload.put("type", type.name());
        payload.put("price", price);
        payload.put("quantity", quantity);
        payload.put("userId", currentUserId);

        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/orders"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(6))
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toJson(payload)));
        applyAuth(b);

        HttpResponse<String> resp = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 201 || resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        }
        throw new RuntimeException(extractErrorMessage(resp.body()));
    }

    public Map<String, Object> cancelOrder(String orderId) throws Exception {
        ensureAuthenticated();
        String uri = String.format("%s/api/orders/%s/cancel?orderId=%s&userId=%s", baseUrl, orderId, orderId, currentUserId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(4))
                .POST(HttpRequest.BodyPublishers.noBody());
        applyAuth(b);

        HttpResponse<String> resp = httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        }
        throw new RuntimeException(extractErrorMessage(resp.body()));
    }

    public Map<String, Object> runDemoScenario() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/demo/run-scenario"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return JsonUtil.getMapper().readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
        }
        throw new RuntimeException(extractErrorMessage(resp.body()));
    }

    public boolean testConnection() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 && resp.body().contains("Real-Time Electronic Stock Exchange");
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureAuthenticated() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Not authenticated. Please log in first.");
        }
    }

    private void applyAuth(HttpRequest.Builder builder) {
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
    }

    private List<Order> parseOrdersList(String json) {
        try {
            JsonNode root = JsonUtil.fromJson(json, JsonNode.class);
            List<Order> list = new ArrayList<>();
            if (root.isArray()) {
                for (JsonNode node : root) {
                    String oid = node.get("orderId").asText();
                    String uid = node.get("userId").asText();
                    String sym = node.get("symbol").asText();
                    String sideStr = node.get("side").asText();
                    String typeStr = node.get("type").asText();
                    double price = node.get("price").asDouble();
                    int qty = node.get("quantity").asInt();
                    int rem = node.get("remainingQuantity").asInt();
                    String statStr = node.get("status").asText();
                    long epoch = node.get("timestampEpoch").asLong();

                    OrderType ot = OrderType.valueOf(typeStr);
                    OrderStatus os = OrderStatus.valueOf(statStr);

                    if ("BUY".equalsIgnoreCase(sideStr)) {
                        list.add(new BuyOrder(oid, uid, sym, ot, price, qty, rem, os, epoch));
                    } else {
                        list.add(new SellOrder(oid, uid, sym, ot, price, qty, rem, os, epoch));
                    }
                }
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode root = JsonUtil.fromJson(responseBody, JsonNode.class);
            if (root.has("message")) {
                return root.get("message").asText();
            }
        } catch (Exception ignored) {}
        return responseBody;
    }
}
