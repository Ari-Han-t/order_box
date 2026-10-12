package com.exchange.ui;

import com.exchange.client.ExchangeApiClient;
import com.exchange.model.OrderSide;
import com.exchange.model.OrderType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Interactive Order Placement Ticket supporting Limit and Market orders
 * with instantaneous validation and purchasing power checks (Presentation Slide 10 / 12).
 */
public class OrderEntryComponent extends VBox {
    private final ExchangeApiClient apiClient;
    private final Consumer<Void> onOrderPlacedCallback;

    private OrderSide selectedSide = OrderSide.BUY;
    private OrderType selectedType = OrderType.LIMIT;
    private String currentSymbol = "AAPL";
    private double currentMarketPrice = 200.0;
    private double userCashBalance = 0.0;
    private int userHoldingShares = 0;

    private final Button btnBuy = new Button("BUY");
    private final Button btnSell = new Button("SELL");
    private final ToggleButton btnLimit = new ToggleButton("LIMIT");
    private final ToggleButton btnMarket = new ToggleButton("MARKET");

    private final TextField priceField = new TextField("200.00");
    private final TextField qtyField = new TextField("10");

    private final Label estimatedTotalLabel = new Label("ESTIMATED TOTAL: $2,000.00");
    private final Label availablePowerLabel = new Label("Available Cash: $0.00");
    private final Label feedbackLabel = new Label();
    private final Button submitBtn = new Button("SUBMIT BUY ORDER");

    public OrderEntryComponent(ExchangeApiClient apiClient, Consumer<Void> onOrderPlacedCallback) {
        this.apiClient = apiClient;
        this.onOrderPlacedCallback = onOrderPlacedCallback;

        setSpacing(10);
        getStyleClass().add("card-panel");
        setMinWidth(280);
        setPrefWidth(320);

        Label header = new Label("ORDER ENTRY TICKET");
        header.getStyleClass().add("panel-header");

        // Buy / Sell Side Selector Buttons
        HBox sideBox = new HBox(8);
        btnBuy.setMaxWidth(Double.MAX_VALUE);
        btnSell.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnBuy, Priority.ALWAYS);
        HBox.setHgrow(btnSell, Priority.ALWAYS);
        btnBuy.getStyleClass().add("btn-buy");
        btnSell.getStyleClass().add("button");

        btnBuy.setOnAction(e -> setSide(OrderSide.BUY));
        btnSell.setOnAction(e -> setSide(OrderSide.SELL));
        sideBox.getChildren().addAll(btnBuy, btnSell);

        // Limit / Market Type Toggle
        HBox typeBox = new HBox(8);
        ToggleGroup typeGroup = new ToggleGroup();
        btnLimit.setToggleGroup(typeGroup);
        btnMarket.setToggleGroup(typeGroup);
        btnLimit.setSelected(true);
        btnLimit.setMaxWidth(Double.MAX_VALUE);
        btnMarket.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnLimit, Priority.ALWAYS);
        HBox.setHgrow(btnMarket, Priority.ALWAYS);

        btnLimit.setOnAction(e -> setType(OrderType.LIMIT));
        btnMarket.setOnAction(e -> setType(OrderType.MARKET));
        typeBox.getChildren().addAll(btnLimit, btnMarket);

        // Price Input
        VBox priceBox = new VBox(4);
        Label priceLbl = new Label("Limit Price ($)");
        priceLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");
        priceBox.getChildren().addAll(priceLbl, priceField);

        // Quantity Input + Quick buttons
        VBox qtyBox = new VBox(4);
        Label qtyLbl = new Label("Quantity (Shares)");
        qtyLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e; -fx-font-weight: bold;");

        HBox quickQtyBox = new HBox(4);
        Button q10 = new Button("10");
        Button q25 = new Button("25");
        Button q50 = new Button("50");
        Button q100 = new Button("100");
        q10.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        q25.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        q50.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        q100.setStyle("-fx-font-size: 10px; -fx-padding: 2px 6px;");
        q10.setOnAction(e -> setQty(10));
        q25.setOnAction(e -> setQty(25));
        q50.setOnAction(e -> setQty(50));
        q100.setOnAction(e -> setQty(100));
        quickQtyBox.getChildren().addAll(q10, q25, q50, q100);

        qtyBox.getChildren().addAll(qtyLbl, qtyField, quickQtyBox);

        // Calculation listeners
        priceField.textProperty().addListener((obs, o, n) -> recalculateTotal());
        qtyField.textProperty().addListener((obs, o, n) -> recalculateTotal());

        // Labels
        estimatedTotalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #58a6ff;");
        availablePowerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
        feedbackLabel.setWrapText(true);
        feedbackLabel.setStyle("-fx-font-size: 11px;");

        // Submit Button
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.getStyleClass().add("btn-buy");
        submitBtn.setOnAction(e -> handleSubmit());

        getChildren().addAll(
                header,
                sideBox,
                typeBox,
                priceBox,
                qtyBox,
                estimatedTotalLabel,
                availablePowerLabel,
                submitBtn,
                feedbackLabel
        );
    }

    private void setSide(OrderSide side) {
        this.selectedSide = side;
        if (side == OrderSide.BUY) {
            btnBuy.getStyleClass().removeAll("button", "btn-buy");
            btnBuy.getStyleClass().add("btn-buy");
            btnSell.getStyleClass().removeAll("btn-sell");
            btnSell.getStyleClass().add("button");
            submitBtn.setText("SUBMIT BUY ORDER");
            submitBtn.getStyleClass().removeAll("btn-sell");
            submitBtn.getStyleClass().add("btn-buy");
        } else {
            btnSell.getStyleClass().removeAll("button", "btn-sell");
            btnSell.getStyleClass().add("btn-sell");
            btnBuy.getStyleClass().removeAll("btn-buy");
            btnBuy.getStyleClass().add("button");
            submitBtn.setText("SUBMIT SELL ORDER");
            submitBtn.getStyleClass().removeAll("btn-buy");
            submitBtn.getStyleClass().add("btn-sell");
        }
        recalculateTotal();
    }

    private void setType(OrderType type) {
        this.selectedType = type;
        if (type == OrderType.MARKET) {
            priceField.setDisable(true);
            priceField.setText("MKT (" + String.format("%.2f", currentMarketPrice) + ")");
        } else {
            priceField.setDisable(false);
            priceField.setText(String.format("%.2f", currentMarketPrice));
        }
        recalculateTotal();
    }

    private void setQty(int q) {
        qtyField.setText(String.valueOf(q));
    }

    public void setSymbolContext(String symbol, double lastPrice) {
        this.currentSymbol = symbol;
        this.currentMarketPrice = lastPrice > 0 ? lastPrice : 100.0;
        if (selectedType == OrderType.LIMIT && (priceField.getText().isEmpty() || priceField.getText().contains("MKT"))) {
            priceField.setText(String.format("%.2f", currentMarketPrice));
        } else if (selectedType == OrderType.MARKET) {
            priceField.setText("MKT (" + String.format("%.2f", currentMarketPrice) + ")");
        }
        recalculateTotal();
    }

    public void setUserContext(double cashBalance, int holdingShares) {
        this.userCashBalance = cashBalance;
        this.userHoldingShares = holdingShares;
        recalculateTotal();
    }

    private void recalculateTotal() {
        try {
            int qty = 0;
            try {
                qty = Integer.parseInt(qtyField.getText().trim());
            } catch (Exception ignored) {}

            double price = currentMarketPrice;
            if (selectedType == OrderType.LIMIT) {
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                } catch (Exception ignored) {}
            }

            double total = price * qty;
            estimatedTotalLabel.setText(String.format("ESTIMATED TOTAL: $%,.2f", total));

            if (selectedSide == OrderSide.BUY) {
                availablePowerLabel.setText(String.format("Available Cash: $%,.2f", userCashBalance));
                if (total > userCashBalance) {
                    availablePowerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f85149;");
                } else {
                    availablePowerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
                }
            } else {
                availablePowerLabel.setText(String.format("Available %s Shares: %,d shares", currentSymbol, userHoldingShares));
                if (qty > userHoldingShares) {
                    availablePowerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f85149;");
                } else {
                    availablePowerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b949e;");
                }
            }
        } catch (Exception ignored) {}
    }

    private void handleSubmit() {
        feedbackLabel.setText("");
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new IllegalArgumentException();
        } catch (Exception e) {
            showFeedback("Invalid quantity: must be a positive integer", false);
            return;
        }

        double price = 0.0;
        if (selectedType == OrderType.LIMIT) {
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0.0) throw new IllegalArgumentException();
            } catch (Exception e) {
                showFeedback("Invalid price: must be greater than zero", false);
                return;
            }
        }

        final double finalPrice = price;
        submitBtn.setDisable(true);

        new Thread(() -> {
            try {
                Map<String, Object> result = apiClient.submitOrder(currentSymbol, selectedSide, selectedType, finalPrice, qty);
                Platform.runLater(() -> {
                    submitBtn.setDisable(false);
                    int tradesCount = (int) result.getOrDefault("tradesCount", 0);
                    boolean fullyFilled = (boolean) result.getOrDefault("fullyFilled", false);
                    if (tradesCount > 0) {
                        showFeedback(String.format("Order matched! %d trade(s) executed%s",
                                tradesCount, fullyFilled ? " (Fully Filled)" : " (Partially Filled)"), true);
                    } else {
                        showFeedback("Order resting in Order Book as Limit order.", true);
                    }
                    if (onOrderPlacedCallback != null) {
                        onOrderPlacedCallback.accept(null);
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    submitBtn.setDisable(false);
                    showFeedback("Error: " + ex.getMessage(), false);
                });
            }
        }).start();
    }

    private void showFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle(String.format("-fx-font-size: 11px; -fx-text-fill: %s; -fx-font-weight: bold;",
                success ? "#3fb950" : "#f85149"));
    }
}
