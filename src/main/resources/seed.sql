-- ====================================================================
-- Seed Data: Sample Stocks, Users, Portfolios, and Initial Book Liquidity
-- ====================================================================

-- 1. Seed Stocks (11 Premier Equities & Tech Market Leaders)
INSERT INTO stocks (symbol, company_name, current_price, previous_close, total_volume)
VALUES 
    ('AAPL', 'Apple Inc.', 200.0000, 198.5000, 142500),
    ('MSFT', 'Microsoft Corporation', 420.0000, 418.0000, 98400),
    ('GOOGL', 'Alphabet Inc.', 180.0000, 179.2000, 81200),
    ('TSLA', 'Tesla Inc.', 250.0000, 246.8000, 168000),
    ('NVDA', 'NVIDIA Corporation', 125.0000, 122.5000, 235000),
    ('AMZN', 'Amazon.com Inc.', 186.5000, 184.2000, 112000),
    ('META', 'Meta Platforms Inc.', 584.0000, 581.0000, 54200),
    ('AMD', 'Advanced Micro Devices', 156.2000, 154.5000, 89100),
    ('PLTR', 'Palantir Technologies', 43.8000, 42.6000, 178000),
    ('COIN', 'Coinbase Global Inc.', 218.4000, 215.0000, 62400),
    ('JPM', 'JPMorgan Chase & Co.', 222.5000, 220.8000, 71500);

-- 2. Seed Users (8 Distinct Trader Personas & Institutional Accounts)
INSERT INTO users (user_id, username, password_hash, full_name, balance)
VALUES
    ('u-marketmaker-001', 'market_maker', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Citadel Securities MM', 10000000.0000),
    ('u-alice-002', 'alice', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Alice Smith (Tech Growth)', 50000.0000),
    ('u-bob-003', 'bob', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Bob Jones (Momentum)', 50000.0000),
    ('u-charlie-004', 'charlie', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Charlie Brown (Value)', 75000.0000),
    ('u-diana-005', 'diana', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Diana Prince (Quantitative)', 150000.0000),
    ('u-edward-006', 'edward', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Edward Norton (Arbitrage)', 95000.0000),
    ('u-fiona-007', 'fiona', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Fiona Gallagher (Day Trader)', 65000.0000),
    ('u-hedgefund-008', 'hedge_fund', '$2a$10$Y1yY4Vb7sMvh8u7dGQhApu3eT/R17MhU4sR8L7xH9Y2/wA8xQ/rGe', 'Apex Horizon Capital', 5000000.0000);

-- 3. Seed Portfolios (Holdings across all symbols for diversified trading)
INSERT INTO portfolios (portfolio_id, user_id, symbol, quantity, average_buy_price)
VALUES
    -- Institutional Market Maker Deep Inventory
    ('p-mm-aapl', 'u-marketmaker-001', 'AAPL', 10000, 195.0000),
    ('p-mm-msft', 'u-marketmaker-001', 'MSFT', 5000, 410.0000),
    ('p-mm-googl', 'u-marketmaker-001', 'GOOGL', 5000, 175.0000),
    ('p-mm-tsla', 'u-marketmaker-001', 'TSLA', 5000, 240.0000),
    ('p-mm-nvda', 'u-marketmaker-001', 'NVDA', 10000, 120.0000),
    ('p-mm-amzn', 'u-marketmaker-001', 'AMZN', 8000, 180.0000),
    ('p-mm-meta', 'u-marketmaker-001', 'META', 3000, 565.0000),
    ('p-mm-amd', 'u-marketmaker-001', 'AMD', 6000, 150.0000),
    ('p-mm-pltr', 'u-marketmaker-001', 'PLTR', 20000, 40.0000),
    ('p-mm-coin', 'u-marketmaker-001', 'COIN', 4000, 205.0000),
    ('p-mm-jpm', 'u-marketmaker-001', 'JPM', 5000, 215.0000),

    -- Alice Smith Holdings
    ('p-alice-aapl', 'u-alice-002', 'AAPL', 150, 198.0000),
    ('p-alice-msft', 'u-alice-002', 'MSFT', 50, 415.0000),
    ('p-alice-nvda', 'u-alice-002', 'NVDA', 120, 121.5000),
    ('p-alice-pltr', 'u-alice-002', 'PLTR', 400, 41.2000),

    -- Bob Jones Holdings
    ('p-bob-aapl', 'u-bob-003', 'AAPL', 200, 197.5000),
    ('p-bob-tsla', 'u-bob-003', 'TSLA', 80, 245.0000),
    ('p-bob-amd', 'u-bob-003', 'AMD', 150, 152.0000),
    ('p-bob-coin', 'u-bob-003', 'COIN', 60, 210.0000),

    -- Charlie Brown Holdings
    ('p-charlie-googl', 'u-charlie-004', 'GOOGL', 100, 178.0000),
    ('p-charlie-amzn', 'u-charlie-004', 'AMZN', 120, 182.5000),
    ('p-charlie-jpm', 'u-charlie-004', 'JPM', 150, 218.0000),

    -- Diana Prince Holdings (Quant)
    ('p-diana-meta', 'u-diana-005', 'META', 80, 575.0000),
    ('p-diana-nvda', 'u-diana-005', 'NVDA', 300, 122.0000),
    ('p-diana-pltr', 'u-diana-005', 'PLTR', 800, 42.0000),

    -- Edward Norton Holdings
    ('p-edward-amd', 'u-edward-006', 'AMD', 200, 153.5000),
    ('p-edward-tsla', 'u-edward-006', 'TSLA', 100, 247.0000),

    -- Fiona Gallagher Holdings
    ('p-fiona-coin', 'u-fiona-007', 'COIN', 120, 212.0000),
    ('p-fiona-aapl', 'u-fiona-007', 'AAPL', 100, 199.0000),

    -- Apex Capital Hedge Fund
    ('p-hf-aapl', 'u-hedgefund-008', 'AAPL', 3000, 196.0000),
    ('p-hf-msft', 'u-hedgefund-008', 'MSFT', 1500, 412.0000),
    ('p-hf-meta', 'u-hedgefund-008', 'META', 1000, 570.0000),
    ('p-hf-jpm', 'u-hedgefund-008', 'JPM', 2000, 216.0000);

-- 4. Seed Baseline Orders (AAPL Order Book matching Presentation Slide 06)
-- Buy side: 199.90 (100), 199.80 (200), 199.70 (150)
-- Sell side: 200.10 (50), 200.20 (100), 200.50 (200)
INSERT INTO orders (order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, status, timestamp_epoch)
VALUES
    ('ord-init-buy-1', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.9000, 100, 100, 'PENDING', 1700000000001),
    ('ord-init-buy-2', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.8000, 200, 200, 'PENDING', 1700000000002),
    ('ord-init-buy-3', 'u-marketmaker-001', 'AAPL', 'BUY', 'LIMIT', 199.7000, 150, 150, 'PENDING', 1700000000003),
    ('ord-init-sell-1', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.1000, 50, 50, 'PENDING', 1700000000004),
    ('ord-init-sell-2', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.2000, 100, 100, 'PENDING', 1700000000005),
    ('ord-init-sell-3', 'u-marketmaker-001', 'AAPL', 'SELL', 'LIMIT', 200.5000, 200, 200, 'PENDING', 1700000000006);

-- 5. Seed Initial Baseline Orders for MSFT, GOOGL, TSLA, NVDA
INSERT INTO orders (order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, status, timestamp_epoch)
VALUES
    ('ord-init-msft-b1', 'u-marketmaker-001', 'MSFT', 'BUY', 'LIMIT', 419.5000, 50, 50, 'PENDING', 1700000000010),
    ('ord-init-msft-s1', 'u-marketmaker-001', 'MSFT', 'SELL', 'LIMIT', 420.5000, 50, 50, 'PENDING', 1700000000011),
    ('ord-init-googl-b1', 'u-marketmaker-001', 'GOOGL', 'BUY', 'LIMIT', 179.5000, 80, 80, 'PENDING', 1700000000020),
    ('ord-init-googl-s1', 'u-marketmaker-001', 'GOOGL', 'SELL', 'LIMIT', 180.5000, 80, 80, 'PENDING', 1700000000021),
    ('ord-init-tsla-b1', 'u-marketmaker-001', 'TSLA', 'BUY', 'LIMIT', 249.0000, 100, 100, 'PENDING', 1700000000030),
    ('ord-init-tsla-s1', 'u-marketmaker-001', 'TSLA', 'SELL', 'LIMIT', 251.0000, 100, 100, 'PENDING', 1700000000031),
    ('ord-init-nvda-b1', 'u-marketmaker-001', 'NVDA', 'BUY', 'LIMIT', 124.5000, 200, 200, 'PENDING', 1700000000040),
    ('ord-init-nvda-s1', 'u-marketmaker-001', 'NVDA', 'SELL', 'LIMIT', 125.5000, 200, 200, 'PENDING', 1700000000041);

-- 6. Seed Active Order Book Liquidity for AMZN, META, AMD, PLTR, COIN, JPM
INSERT INTO orders (order_id, user_id, symbol, side, type, price, quantity, remaining_quantity, status, timestamp_epoch)
VALUES
    -- AMZN Book ($186.50 mid)
    ('ord-init-amzn-b1', 'u-marketmaker-001', 'AMZN', 'BUY', 'LIMIT', 186.2000, 120, 120, 'PENDING', 1700000000050),
    ('ord-init-amzn-b2', 'u-marketmaker-001', 'AMZN', 'BUY', 'LIMIT', 185.8000, 150, 150, 'PENDING', 1700000000051),
    ('ord-init-amzn-s1', 'u-marketmaker-001', 'AMZN', 'SELL', 'LIMIT', 186.8000, 100, 100, 'PENDING', 1700000000052),
    ('ord-init-amzn-s2', 'u-marketmaker-001', 'AMZN', 'SELL', 'LIMIT', 187.2000, 140, 140, 'PENDING', 1700000000053),

    -- META Book ($584.00 mid)
    ('ord-init-meta-b1', 'u-marketmaker-001', 'META', 'BUY', 'LIMIT', 583.5000, 40, 40, 'PENDING', 1700000000060),
    ('ord-init-meta-b2', 'u-marketmaker-001', 'META', 'BUY', 'LIMIT', 582.0000, 60, 60, 'PENDING', 1700000000061),
    ('ord-init-meta-s1', 'u-marketmaker-001', 'META', 'SELL', 'LIMIT', 584.5000, 45, 45, 'PENDING', 1700000000062),
    ('ord-init-meta-s2', 'u-marketmaker-001', 'META', 'SELL', 'LIMIT', 585.8000, 70, 70, 'PENDING', 1700000000063),

    -- AMD Book ($156.20 mid)
    ('ord-init-amd-b1', 'u-marketmaker-001', 'AMD', 'BUY', 'LIMIT', 155.8000, 90, 90, 'PENDING', 1700000000070),
    ('ord-init-amd-b2', 'u-marketmaker-001', 'AMD', 'BUY', 'LIMIT', 155.0000, 130, 130, 'PENDING', 1700000000071),
    ('ord-init-amd-s1', 'u-marketmaker-001', 'AMD', 'SELL', 'LIMIT', 156.6000, 85, 85, 'PENDING', 1700000000072),
    ('ord-init-amd-s2', 'u-marketmaker-001', 'AMD', 'SELL', 'LIMIT', 157.2000, 110, 110, 'PENDING', 1700000000073),

    -- PLTR Book ($43.80 mid)
    ('ord-init-pltr-b1', 'u-marketmaker-001', 'PLTR', 'BUY', 'LIMIT', 43.6000, 500, 500, 'PENDING', 1700000000080),
    ('ord-init-pltr-b2', 'u-marketmaker-001', 'PLTR', 'BUY', 'LIMIT', 43.3000, 750, 750, 'PENDING', 1700000000081),
    ('ord-init-pltr-s1', 'u-marketmaker-001', 'PLTR', 'SELL', 'LIMIT', 44.0000, 450, 450, 'PENDING', 1700000000082),
    ('ord-init-pltr-s2', 'u-marketmaker-001', 'PLTR', 'SELL', 'LIMIT', 44.4000, 600, 600, 'PENDING', 1700000000083),

    -- COIN Book ($218.40 mid)
    ('ord-init-coin-b1', 'u-marketmaker-001', 'COIN', 'BUY', 'LIMIT', 217.8000, 60, 60, 'PENDING', 1700000000090),
    ('ord-init-coin-b2', 'u-marketmaker-001', 'COIN', 'BUY', 'LIMIT', 216.5000, 90, 90, 'PENDING', 1700000000091),
    ('ord-init-coin-s1', 'u-marketmaker-001', 'COIN', 'SELL', 'LIMIT', 218.9000, 55, 55, 'PENDING', 1700000000092),
    ('ord-init-coin-s2', 'u-marketmaker-001', 'COIN', 'SELL', 'LIMIT', 219.8000, 80, 80, 'PENDING', 1700000000093),

    -- JPM Book ($222.50 mid)
    ('ord-init-jpm-b1', 'u-marketmaker-001', 'JPM', 'BUY', 'LIMIT', 222.0000, 80, 80, 'PENDING', 1700000000100),
    ('ord-init-jpm-b2', 'u-marketmaker-001', 'JPM', 'BUY', 'LIMIT', 221.2000, 120, 120, 'PENDING', 1700000000101),
    ('ord-init-jpm-s1', 'u-marketmaker-001', 'JPM', 'SELL', 'LIMIT', 223.0000, 75, 75, 'PENDING', 1700000000102),
    ('ord-init-jpm-s2', 'u-marketmaker-001', 'JPM', 'SELL', 'LIMIT', 223.8000, 110, 110, 'PENDING', 1700000000103);
