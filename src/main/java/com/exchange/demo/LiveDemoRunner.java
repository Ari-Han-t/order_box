package com.exchange.demo;

import com.exchange.client.ExchangeApiClient;
import com.exchange.server.EmbeddedServletContainer;
import com.exchange.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Headless CLI Demo Runner executing the full presentation scenario
 * against the backend and database.
 */
public class LiveDemoRunner {
    private static final Logger logger = LoggerFactory.getLogger(LiveDemoRunner.class);

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   REAL-TIME ELECTRONIC STOCK EXCHANGE — SCRIPTED DEMO SCENARIO          ");
        System.out.println("   (Presentation Slides 07 & 11: Maker Price, Partial Fill & Priority)   ");
        System.out.println("==========================================================================");

        String baseUrl = System.getProperty("exchange.url", "http://localhost:8085");
        ExchangeApiClient client = new ExchangeApiClient(baseUrl);

        EmbeddedServletContainer embeddedServer = null;
        if (!client.testConnection()) {
            System.out.println("[INFO] Local server not active on port 8085. Booting embedded Exchange...");
            try {
                embeddedServer = new EmbeddedServletContainer(8085);
                embeddedServer.start();
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to start server: " + e.getMessage());
                System.exit(1);
            }
        }

        try {
            System.out.println("\n[1/3] Triggering Presentation Demo Scenario over REST API...");
            Map<String, Object> result = client.runDemoScenario();

            System.out.println("\n[2/3] Verification Results:");
            System.out.println(JsonUtil.toPrettyJson(result));

            System.out.println("\n[3/3] Scenario Completed Successfully!");
            System.out.println("==========================================================================");
        } catch (Exception e) {
            System.err.println("[ERROR] Scenario execution failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (embeddedServer != null) {
                try {
                    embeddedServer.stop();
                } catch (Exception ignored) {}
            }
            System.exit(0);
        }
    }
}
