-- ====================================================================
-- Real-Time Stock Exchange Database Schema (MySQL & H2 MySQL-Mode Compatible)
-- ====================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    balance DECIMAL(18, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS stocks (
    symbol VARCHAR(10) PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    current_price DECIMAL(18, 4) NOT NULL,
    previous_close DECIMAL(18, 4) NOT NULL,
    total_volume BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    price DECIMAL(18, 4) NOT NULL,
    quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    timestamp_epoch BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_order_stock FOREIGN KEY (symbol) REFERENCES stocks(symbol)
);

CREATE TABLE IF NOT EXISTS trades (
    trade_id VARCHAR(36) PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    buy_order_id VARCHAR(36) NOT NULL,
    sell_order_id VARCHAR(36) NOT NULL,
    buyer_id VARCHAR(36) NOT NULL,
    seller_id VARCHAR(36) NOT NULL,
    price DECIMAL(18, 4) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(18, 4) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_symbol FOREIGN KEY (symbol) REFERENCES stocks(symbol),
    CONSTRAINT fk_trade_buyer FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    CONSTRAINT fk_trade_seller FOREIGN KEY (seller_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS portfolios (
    portfolio_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    average_buy_price DECIMAL(18, 4) NOT NULL DEFAULT 0.0000,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_stock UNIQUE (user_id, symbol),
    CONSTRAINT fk_portfolio_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_portfolio_stock FOREIGN KEY (symbol) REFERENCES stocks(symbol)
);
