package com.exchange;

import com.exchange.client.ExchangeApiClient;
import com.exchange.server.EmbeddedServletContainer;
import com.exchange.ui.TradingDashboardView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX Application launching the Trading Dashboard Desktop UI.
 * Features zero-friction local execution: if a standalone Exchange server
 * is not detected on port 8080, it automatically launches the embedded backend
 * in a daemon thread.
 */
public class ExchangeClientApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeClientApp.class);
    private static EmbeddedServletContainer autoStartedServer;

    private TradingDashboardView dashboardView;

    @Override
    public void start(Stage primaryStage) {
        String baseUrl = System.getProperty("exchange.url", "http://localhost:8085");
        ExchangeApiClient apiClient = new ExchangeApiClient(baseUrl);

        // Check if server is running; if not, spin up embedded server
        if (!apiClient.testConnection()) {
            logger.info("No active Exchange server found on {}. Auto-launching embedded backend...", baseUrl);
            try {
                autoStartedServer = new EmbeddedServletContainer(8085);
                autoStartedServer.start();
                Thread.sleep(500);
            } catch (Exception e) {
                logger.error("Failed to auto-start embedded backend: {}", e.getMessage(), e);
            }
        }

        // Auto-login default seeded user Alice for immediate dashboard experience
        try {
            apiClient.login("alice", "password123");
        } catch (Exception e) {
            logger.warn("Auto-login notice: {}", e.getMessage());
        }

        dashboardView = new TradingDashboardView(primaryStage, apiClient);
        dashboardView.updateUserDisplay();

        Scene scene = new Scene(dashboardView, 1260, 780);
        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        primaryStage.setTitle("Real-Time Stock Exchange Order Book & Matching Engine");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);

        primaryStage.setOnCloseRequest(e -> {
            dashboardView.shutdown();
            if (autoStartedServer != null) {
                try {
                    autoStartedServer.stop();
                } catch (Exception ignored) {}
            }
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
