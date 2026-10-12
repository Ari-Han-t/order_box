-- ====================================================================
-- Seed Data: Sample Stocks, Users, Portfolios, and Initial Book Liquidity
-- ====================================================================

INSERT INTO stocks (symbol, company_name, current_price, previous_close, total_volume)
VALUES 
    ('AAPL', 'Apple Inc.', 200.0000, 198.5000, 125000),
    ('MSFT', 'Microsoft Corporation', 420.0000, 418.0000, 89000),
    ('GOOGL', 'Alphabet Inc.', 180.0000, 179.2000, 74000),
    ('TSLA', 'Tesla Inc.', 250.0000, 246.8000, 153000),
    ('NVDA', 'NVIDIA Corporation', 125.0000, 122.5000, 210000)
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name);

INSERT INTO users (user_id, username, password_hash, full_name, balance)
VALUES
    ('u-marketmaker-001', 'market_maker', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Institutional Market Maker', 1000000.0000),
    ('u-alice-002', 'alice', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Alice Smith', 50000.0000),
    ('u-bob-003', 'bob', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Bob Jones', 50000.0000),
    ('u-charlie-004', 'charlie', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Charlie Brown', 75000.0000),
    ('u-demo-005', 'demo_trader', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Demo Trader', 100000.0000)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

INSERT INTO portfolios (portfolio_id, user_id, symbol, quantity, average_buy_price)
VALUES
    ('p-mm-aapl', 'u-marketmaker-001', 'AAPL', 10000, 195.0000),
    ('p-mm-msft', 'u-marketmaker-001', 'MSFT', 5000, 410.0000),
    ('p-mm-googl', 'u-marketmaker-001', 'GOOGL', 5000, 175.0000),
    ('p-mm-tsla', 'u-marketmaker-001', 'TSLA', 5000, 240.0000),
    ('p-mm-nvda', 'u-marketmaker-001', 'NVDA', 10000, 120.0000),
    ('p-alice-aapl', 'u-alice-002', 'AAPL', 150, 198.0000),
    ('p-alice-msft', 'u-alice-002', 'MSFT', 50, 415.0000),
    ('p-bob-aapl', 'u-bob-003', 'AAPL', 200, 197.5000),
    ('p-bob-tsla', 'u-bob-003', 'TSLA', 80, 245.0000),
    ('p-charlie-googl', 'u-charlie-004', 'GOOGL', 100, 178.0000),
    ('p-charlie-nvda', 'u-charlie-004', 'NVDA', 250, 121.0000),
    ('p-demo-aapl', 'u-demo-005', 'AAPL', 100, 199.0000)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

INSERT INTO orders (order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, status, timestamp_epoch)
VALUES
    ('ord-init-buy-1', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.9000, 100, 100, 'PENDING', 1700000000001),
    ('ord-init-buy-2', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.8000, 200, 200, 'PENDING', 1700000000002),
    ('ord-init-buy-3', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.7000, 150, 150, 'PENDING', 1700000000003),
    ('ord-init-sell-1', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.1000, 50, 50, 'PENDING', 1700000000004),
    ('ord-init-sell-2', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.2000, 100, 100, 'PENDING', 1700000000005),
    ('ord-init-sell-3', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.5000, 200, 200, 'PENDING', 1700000000006),
    ('ord-init-msft-b1', 'u-marketmaker-001', 'MSFT', 'BUY', 'LIMIT', 419.5000, 50, 50, 'PENDING', 1700000000010),
    ('ord-init-msft-s1', 'u-marketmaker-001', 'MSFT', 'SELL', 'LIMIT', 420.5000, 50, 50, 'PENDING', 1700000000011),
    ('ord-init-googl-b1', 'u-marketmaker-001', 'GOOGL', 'BUY', 'LIMIT', 179.5000, 80, 80, 'PENDING', 1700000000020),
    ('ord-init-googl-s1', 'u-marketmaker-001', 'GOOGL', 'SELL', 'LIMIT', 180.5000, 80, 80, 'PENDING', 1700000000021),
    ('ord-init-tsla-b1', 'u-marketmaker-001', 'TSLA', 'BUY', 'LIMIT', 249.0000, 100, 100, 'PENDING', 1700000000030),
    ('ord-init-tsla-s1', 'u-marketmaker-001', 'TSLA', 'SELL', 'LIMIT', 251.0000, 100, 100, 'PENDING', 1700000000031),
    ('ord-init-nvda-b1', 'u-marketmaker-001', 'NVDA', 'BUY', 'LIMIT', 124.5000, 200, 200, 'PENDING', 1700000000040),
    ('ord-init-nvda-s1', 'u-marketmaker-001', 'NVDA', 'SELL', 'LIMIT', 125.5000, 200, 200, 'PENDING', 1700000000041)
ON DUPLICATE KEY UPDATE status = VALUES(status);
