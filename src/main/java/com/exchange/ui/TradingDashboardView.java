package com.exchange.ui;

import com.exchange.client.ExchangeApiClient;
import com.exchange.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main JavaFX Trading Dashboard View (Presentation Slide 06 / 12 & Slide 10 / 12).
 * Integrates real-time Order Book depth, Live Trades Feed, Interactive Order Entry,
 * Portfolio Asset Valuations, Open Orders with Cancellation, and Scripted Demo Execution.
 */
public class TradingDashboardView extends BorderPane {
    private final Stage primaryStage;
    private final ExchangeApiClient apiClient;

    private String currentSymbol = "AAPL";
    private double currentMarketPrice = 200.0;

    // Header Controls
    private final ComboBox<String> symbolSelector = new ComboBox<>();
    private final Label priceQuoteLabel = new Label("$200.00 (+0.75%)");
    private final Label userStatusPill = new Label("Trader: Guest");
    private final Label connStatusPill = new Label("• LIVE");

    // Sub-components
    private OrderBookComponent orderBookComponent;
    private TradesComponent tradesComponent;
    private OrderEntryComponent orderEntryComponent;
    private PortfolioComponent portfolioComponent;
    private OpenOrdersComponent openOrdersComponent;

    private ScheduledExecutorService pollingExecutor;

    public TradingDashboardView(Stage primaryStage, ExchangeApiClient apiClient) {
        this.primaryStage = primaryStage;
        this.apiClient = apiClient;

        initUI();
        startDataPolling();
    }

    private void initUI() {
        getStyleClass().add("root");
        setPadding(new Insets(0));

        // 1. Build Top Header
        setTop(buildHeader());

        // 2. Initialize Subcomponents
        orderBookComponent = new OrderBookComponent();
        tradesComponent = new TradesComponent();
        orderEntryComponent = new OrderEntryComponent(apiClient, v -> refreshAllData());
        portfolioComponent = new PortfolioComponent();
        openOrdersComponent = new OpenOrdersComponent(apiClient, v -> refreshAllData());

        // 3. Left Column: Order Book + Recent Trades
        VBox leftColumn = new VBox(10);
        leftColumn.setPadding(new Insets(10));
        leftColumn.getChildren().addAll(orderBookComponent, tradesComponent);
        VBox.setVgrow(orderBookComponent, Priority.ALWAYS);
        VBox.setVgrow(tradesComponent, Priority.ALWAYS);

        // 4. Middle Column: Order Entry Ticket + Quick Scenario Card
        VBox centerColumn = new VBox(10);
        centerColumn.setPadding(new Insets(10));

        VBox scenarioPromoCard = buildScenarioCard();
        centerColumn.getChildren().addAll(orderEntryComponent, scenarioPromoCard);
        VBox.setVgrow(orderEntryComponent, Priority.ALWAYS);

        // 5. Right Column: Portfolio & Positions + Open Orders Table
        VBox rightColumn = new VBox(10);
        rightColumn.setPadding(new Insets(10));
        rightColumn.getChildren().addAll(portfolioComponent, openOrdersComponent);
        VBox.setVgrow(portfolioComponent, Priority.ALWAYS);
        VBox.setVgrow(openOrdersComponent, Priority.ALWAYS);

        // Main 3-Column HBox Layout with ScrollPane support
        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(6));
        mainContent.getChildren().addAll(leftColumn, centerColumn, rightColumn);
        HBox.setHgrow(leftColumn, Priority.SOMETIMES);
        HBox.setHgrow(centerColumn, Priority.SOMETIMES);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        ScrollPane scrollWrapper = new ScrollPane(mainContent);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background: #0d1117; -fx-background-color: #0d1117; -fx-border-color: transparent;");

        setCenter(scrollWrapper);
    }

    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-header");

        VBox titleBox = new VBox(1);
        Label appName = new Label("EXCHANGE TRADING TERMINAL");
        appName.getStyleClass().add("app-title");
        Label subtitle = new Label("Real-Time Order Book · Price-Time Priority Matching Engine");
        subtitle.getStyleClass().add("app-subtitle");
        titleBox.getChildren().addAll(appName, subtitle);

        Separator s1 = new Separator();
        s1.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Symbol Selector Dropdown
        symbolSelector.getItems().addAll("AAPL", "MSFT", "GOOGL", "TSLA", "NVDA");
        symbolSelector.setValue("AAPL");
        symbolSelector.setOnAction(e -> {
            currentSymbol = symbolSelector.getValue();
            refreshAllData();
        });

        // Price Quote Banner
        priceQuoteLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3fb950;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Scripted Demo Runner Button
        Button demoBtn = new Button("▶ RUN PRESENTATION SCENARIO");
        demoBtn.getStyleClass().add("btn-demo");
        demoBtn.setOnAction(e -> {
            new DemoScenarioDialog(apiClient, this::refreshAllData).show(primaryStage);
        });

        // User Status / Switcher
        userStatusPill.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #c9d1d9; -fx-background-color: #21262d; -fx-padding: 4px 8px; -fx-background-radius: 4px;");

        Button switchUserBtn = new Button("SWITCH TRADER");
        switchUserBtn.setOnAction(e -> {
            new LoginDialog(apiClient, session -> {
                updateUserDisplay();
                refreshAllData();
            }).show(primaryStage);
        });

        connStatusPill.getStyleClass().add("status-pill-online");

        Button refreshBtn = new Button("↻");
        refreshBtn.setTooltip(new Tooltip("Refresh Market Data"));
        refreshBtn.setOnAction(e -> refreshAllData());

        header.getChildren().addAll(
                titleBox, s1,
                new Label("SYMBOL:"), symbolSelector,
                priceQuoteLabel, spacer,
                demoBtn, userStatusPill, switchUserBtn,
                connStatusPill, refreshBtn
        );

        return header;
    }

    private VBox buildScenarioCard() {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-panel");

        Label title = new Label("LIVE DEMONSTRATION WORKFLOW");
        title.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");

        Label bullet1 = new Label("• Price-Time Priority: Orders matched by Best Price, then FIFO timestamp.");
        Label bullet2 = new Label("• Maker-Price Rule: Trade executes at the resting order's price.");
        Label bullet3 = new Label("• Partial Fill: Unmatched balance remains visible on the Order Book.");
        bullet1.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
        bullet2.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
        bullet3.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");

        Button quickRun = new Button("EXECUTE SLIDE 07 & 11 SCENARIO");
        quickRun.setMaxWidth(Double.MAX_VALUE);
        quickRun.getStyleClass().add("btn-demo");
        quickRun.setOnAction(e -> new DemoScenarioDialog(apiClient, this::refreshAllData).show(primaryStage));

        card.getChildren().addAll(title, bullet1, bullet2, bullet3, quickRun);
        return card;
    }

    public void updateUserDisplay() {
        if (apiClient.isAuthenticated()) {
            userStatusPill.setText("Trader: " + apiClient.getCurrentUsername());
        } else {
            userStatusPill.setText("Trader: Guest");
        }
    }

    private void startDataPolling() {
        pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ExchangeUI-PollingThread");
            t.setDaemon(true);
            return t;
        });

        // Poll every 1200ms
        pollingExecutor.scheduleWithFixedDelay(this::refreshAllData, 200, 1200, TimeUnit.MILLISECONDS);
    }

    public void refreshAllData() {
        try {
            // 1. Fetch Order Book Snapshot
            OrderBookSnapshot bookSnapshot = apiClient.getOrderBook(currentSymbol, 12);

            // 2. Fetch Recent Trades
            List<Trade> trades = apiClient.getRecentTrades(currentSymbol, 20);

            // 3. Fetch Stocks for quote
            List<Stock> stocks = apiClient.getStocks();
            Stock currentStock = null;
            for (Stock s : stocks) {
                if (s.getSymbol().equalsIgnoreCase(currentSymbol)) {
                    currentStock = s;
                    break;
                }
            }

            // 4. Fetch User Portfolio (if authenticated)
            Map<String, Object> portfolioSummary = null;
            List<Order> activeOrders = null;
            double userCash = 0.0;
            int userShares = 0;

            if (apiClient.isAuthenticated()) {
                try {
                    portfolioSummary = apiClient.getPortfolio();
                    activeOrders = apiClient.getActiveOrders();
                    if (portfolioSummary != null) {
                        userCash = ((Number) portfolioSummary.getOrDefault("cashBalance", 0.0)).doubleValue();
                        List<Map<String, Object>> holdings = (List<Map<String, Object>>) portfolioSummary.get("holdings");
                        if (holdings != null) {
                            for (Map<String, Object> h : holdings) {
                                if (currentSymbol.equalsIgnoreCase((String) h.get("symbol"))) {
                                    userShares = ((Number) h.get("quantity")).intValue();
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            // Update UI on JavaFX thread
            final Stock finalStock = currentStock;
            final Map<String, Object> finalPortfolio = portfolioSummary;
            final List<Order> finalOrders = activeOrders;
            final double finalUserCash = userCash;
            final int finalUserShares = userShares;

            Platform.runLater(() -> {
                orderBookComponent.update(bookSnapshot);
                tradesComponent.update(trades);

                if (finalStock != null) {
                    currentMarketPrice = finalStock.getCurrentPrice();
                    double chg = finalStock.getChangePercent();
                    priceQuoteLabel.setText(String.format("$%.2f (%+.2f%%)", currentMarketPrice, chg));
                    priceQuoteLabel.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;",
                            chg >= 0 ? "#3fb950" : "#f85149"));
                } else if (bookSnapshot != null && bookSnapshot.getLastPrice() > 0) {
                    currentMarketPrice = bookSnapshot.getLastPrice();
                    priceQuoteLabel.setText(String.format("$%.2f", currentMarketPrice));
                }

                orderEntryComponent.setSymbolContext(currentSymbol, currentMarketPrice);
                orderEntryComponent.setUserContext(finalUserCash, finalUserShares);

                if (finalPortfolio != null) {
                    portfolioComponent.update(finalPortfolio);
                }
                if (finalOrders != null) {
                    openOrdersComponent.update(finalOrders);
                }

                connStatusPill.setText("• LIVE");
                connStatusPill.getStyleClass().clear();
                connStatusPill.getStyleClass().add("status-pill-online");
            });

        } catch (Exception ex) {
            Platform.runLater(() -> {
                connStatusPill.setText("• OFFLINE");
                connStatusPill.setStyle("-fx-text-fill: #f85149; -fx-font-size: 11px;");
            });
        }
    }

    public void shutdown() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdownNow();
        }
    }
}
