package com.exchange;

import com.exchange.server.EmbeddedServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Entry Point for the Exchange Backend Server.
 * Initializes database connectivity, starts the embedded Jakarta Servlet container,
 * and maintains the matching engine for concurrent trading operations.
 */
public class ExchangeServerApp {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeServerApp.class);

    public static void main(String[] args) {
        int port = 8085;
        String envPort = System.getenv("PORT");
        if (envPort == null) envPort = System.getenv("EXCHANGE_PORT");
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        try {
            EmbeddedServletContainer container = new EmbeddedServletContainer(port);
            container.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Initiating graceful shutdown of Exchange Server...");
                try {
                    container.stop();
                } catch (Exception e) {
                    logger.error("Error during server shutdown: {}", e.getMessage());
                }
            }));

            // Keep main thread alive
            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Failed to start Exchange Server: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
