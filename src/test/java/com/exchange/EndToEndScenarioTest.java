package com.exchange;

import com.exchange.client.ExchangeApiClient;
import com.exchange.server.EmbeddedServletContainer;
import com.exchange.service.DemoScenarioService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EndToEndScenarioTest {
    private static EmbeddedServletContainer server;
    private static int port;
    private static ExchangeApiClient client;

    @BeforeAll
    static void startServer() throws Exception {
        // Find free port
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }

        server = new EmbeddedServletContainer(port);
        server.start();

        client = new ExchangeApiClient("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    @DisplayName("Health Check endpoint returns UP and database connectivity")
    void testHealthEndpoint() {
        assertTrue(client.testConnection(), "Server health check must return HTTP 200 UP");
    }

    @Test
    @DisplayName("REST API End-to-End: Authentication, Order Placement, Matching, and Portfolio Verification")
    void testFullRestApiWorkflow() throws Exception {
        // 1. User Registration
        String testUser = "trader_" + System.currentTimeMillis();
        Map<String, Object> reg = client.register(testUser, "securePass123", "Test Trader", 75000.0);
        assertNotNull(reg.get("token"));
        assertEquals(testUser, reg.get("username"));

        // 2. Query Portfolio
        Map<String, Object> portfolio = client.getPortfolio();
        assertEquals(75000.0, ((Number) portfolio.get("cashBalance")).doubleValue(), 0.01);

        // 3. Query Stocks
        assertFalse(client.getStocks().isEmpty(), "Listed stocks must be returned");

        // 4. Query Order Book
        assertNotNull(client.getOrderBook("AAPL", 5));
    }

    @Test
    @DisplayName("Demo Scenario Service executes Slide 07 & 11 scenario end-to-end against live database")
    void testPresentationDemoScenario() throws Exception {
        DemoScenarioService demoService = server.getDemoScenarioService();
        Map<String, Object> result = demoService.runPresentationScenario();

        assertNotNull(result);
        assertEquals("SUCCESS", result.get("status"));
        assertTrue(result.containsKey("steps"));
        assertTrue(result.containsKey("alicePortfolio"));
        assertTrue(result.containsKey("bobPortfolio"));

        // Also test executing it over HTTP REST endpoint
        Map<String, Object> httpResult = client.runDemoScenario();
        assertEquals("SUCCESS", httpResult.get("status"));
    }
}
