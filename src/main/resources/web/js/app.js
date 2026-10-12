/**
 * AURA EXCHANGE — FINANCIAL TRADING DASHBOARD CLIENT SCRIPT
 * Handles real-time polling, L2 Order Book rendering, order execution,
 * portfolio tracking, and scripted presentation demo scenario visualizer.
 */

(() => {
    // Application State
    const state = {
        symbol: 'AAPL',
        trader: 'alice',
        side: 'BUY',
        orderType: 'LIMIT',
        stocks: {},
        lastPrice: 200.00,
        portfolio: null,
        isPolling: true
    };

    // DOM Elements
    const elements = {
        symbolTabs: document.getElementById('symbol-tabs-container'),
        traderSelect: document.getElementById('trader-select'),
        runDemoBtn: document.getElementById('run-demo-btn'),
        engineStatusPill: document.getElementById('engine-status-pill'),
        engineStatusText: document.getElementById('engine-status-text'),

        // Order Book
        obSymbolBadge: document.getElementById('ob-symbol-badge'),
        asksContainer: document.getElementById('orderbook-asks-container'),
        bidsContainer: document.getElementById('orderbook-bids-container'),
        spreadValue: document.getElementById('spread-value'),
        spreadMid: document.getElementById('spread-mid'),
        depthBarBid: document.getElementById('depth-bar-bid-fill'),
        depthBarAsk: document.getElementById('depth-bar-ask-fill'),
        bidTotalShares: document.getElementById('bid-total-shares'),
        askTotalShares: document.getElementById('ask-total-shares'),

        // Hero
        heroCompany: document.getElementById('hero-company-name'),
        heroSymbolTicker: document.getElementById('hero-symbol-ticker'),
        heroLastPrice: document.getElementById('hero-last-price'),
        heroPriceChange: document.getElementById('hero-price-change'),
        heroBestBid: document.getElementById('hero-best-bid'),
        heroBestAsk: document.getElementById('hero-best-ask'),
        heroVolume: document.getElementById('hero-volume'),

        // Order Ticket
        sideBuyBtn: document.getElementById('side-buy-btn'),
        sideSellBtn: document.getElementById('side-sell-btn'),
        orderTypeSelect: document.getElementById('order-type-select'),
        priceInputGroup: document.getElementById('price-input-group'),
        orderPriceInput: document.getElementById('order-price-input'),
        orderQtyInput: document.getElementById('order-qty-input'),
        availableHint: document.getElementById('available-resource-hint'),
        orderEstTotal: document.getElementById('order-est-total'),
        submitOrderBtn: document.getElementById('submit-order-btn'),
        orderAlert: document.getElementById('order-feedback-alert'),

        // Trades & Orders
        tabTradesFeed: document.getElementById('tab-trades-feed'),
        tabMyOrders: document.getElementById('tab-my-orders'),
        contentTradesFeed: document.getElementById('content-trades-feed'),
        contentMyOrders: document.getElementById('content-my-orders'),
        tradesFeedContainer: document.getElementById('trades-feed-container'),
        myOpenOrdersContainer: document.getElementById('my-open-orders-container'),
        openOrdersCount: document.getElementById('open-orders-count'),

        // Portfolio
        portUserFullname: document.getElementById('port-user-fullname'),
        portCashVal: document.getElementById('port-cash-val'),
        portHoldingsVal: document.getElementById('port-holdings-val'),
        portNetworthVal: document.getElementById('port-networth-val'),
        portPnlVal: document.getElementById('port-pnl-val'),
        holdingsTableBody: document.getElementById('holdings-table-body'),

        // Modal
        demoModal: document.getElementById('demo-modal'),
        demoModalBody: document.getElementById('demo-modal-body'),
        modalCloseBtn: document.getElementById('modal-close-btn'),
        modalDismissBtn: document.getElementById('modal-dismiss-btn'),
        modalRerunBtn: document.getElementById('modal-rerun-btn')
    };

    // Helper: Formatter utilities
    const formatCurrency = (val) => '$' + (Number(val) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const formatNumber = (val) => (Number(val) || 0).toLocaleString('en-US');

    // =========================================================================
    // API Client Calls
    // =========================================================================
    async function apiRequest(endpoint, options = {}) {
        const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
        if (state.trader) {
            headers['X-User-Id'] = state.trader;
        }
        const resp = await fetch(endpoint, { ...options, headers });
        if (!resp.ok) {
            const errBody = await resp.json().catch(() => ({ message: resp.statusText }));
            throw new Error(errBody.error || errBody.message || 'API request failed');
        }
        return resp.json();
    }

    // =========================================================================
    // Core Data Fetchers
    // =========================================================================
    async function fetchStocks() {
        try {
            const stocks = await apiRequest('/api/stocks');
            stocks.forEach(stock => {
                state.stocks[stock.symbol] = stock;
                const tabPrice = document.getElementById(`nav-price-${stock.symbol}`);
                if (tabPrice) tabPrice.textContent = formatCurrency(stock.currentPrice);
            });

            const current = state.stocks[state.symbol];
            if (current) {
                elements.heroCompany.textContent = current.companyName;
                elements.heroSymbolTicker.textContent = `${current.symbol} / USD`;
                const changePct = current.changePercent || 0;
                elements.heroPriceChange.textContent = `${changePct >= 0 ? '+' : ''}${changePct.toFixed(2)}%`;
                elements.heroPriceChange.className = `hero-badge ${changePct >= 0 ? 'badge-up' : 'badge-down'}`;
            }
        } catch (e) {
            console.warn('Stocks fetch error:', e.message);
        }
    }

    async function fetchOrderBook() {
        try {
            const data = await apiRequest(`/api/orderbook?symbol=${state.symbol}&depth=12`);
            renderOrderBook(data);
        } catch (e) {
            console.warn('OrderBook fetch error:', e.message);
        }
    }

    async function fetchTrades() {
        try {
            const trades = await apiRequest(`/api/trades?symbol=${state.symbol}&limit=25`);
            renderTrades(trades);
        } catch (e) {
            console.warn('Trades fetch error:', e.message);
        }
    }

    async function fetchPortfolio() {
        try {
            const port = await apiRequest('/api/portfolio');
            state.portfolio = port;
            renderPortfolio(port);
        } catch (e) {
            console.warn('Portfolio fetch error:', e.message);
        }
    }

    async function fetchOpenOrders() {
        try {
            const orders = await apiRequest('/api/orders?activeOnly=true');
            renderOpenOrders(orders);
        } catch (e) {
            console.warn('Open orders fetch error:', e.message);
        }
    }

    // =========================================================================
    // Rendering Functions
    // =========================================================================
    function renderOrderBook(book) {
        elements.obSymbolBadge.textContent = state.symbol;
        if (!book) return;

        const bids = book.bids || [];
        const asks = book.asks || [];

        const bestBid = bids.length > 0 ? bids[0].price : null;
        const bestAsk = asks.length > 0 ? asks[0].price : null;

        elements.heroBestBid.textContent = bestBid ? formatCurrency(bestBid) : '---';
        elements.heroBestAsk.textContent = bestAsk ? formatCurrency(bestAsk) : '---';
        if (book.lastPrice) {
            elements.heroLastPrice.textContent = formatCurrency(book.lastPrice);
        }

        // Spread Calculation
        if (bestBid && bestAsk) {
            const spread = bestAsk - bestBid;
            const spreadPct = (spread / bestAsk) * 100;
            const mid = (bestBid + bestAsk) / 2;
            elements.spreadValue.textContent = `${formatCurrency(spread)} (${spreadPct.toFixed(2)}%)`;
            elements.spreadMid.textContent = `MID ${formatCurrency(mid)}`;
        } else {
            elements.spreadValue.textContent = 'NO ACTIVE SPREAD';
            elements.spreadMid.textContent = '';
        }

        // Total Volumes for visual depth calculation
        const totalBidVol = bids.reduce((acc, b) => acc + b.quantity, 0);
        const totalAskVol = asks.reduce((acc, a) => acc + a.quantity, 0);
        const maxVol = Math.max(totalBidVol, totalAskVol, 1);

        elements.bidTotalShares.textContent = `Bids: ${formatNumber(totalBidVol)} sh`;
        elements.askTotalShares.textContent = `Asks: ${formatNumber(totalAskVol)} sh`;

        const totalDepth = totalBidVol + totalAskVol;
        if (totalDepth > 0) {
            const bidPct = Math.round((totalBidVol / totalDepth) * 100);
            elements.depthBarBid.style.width = `${bidPct}%`;
            elements.depthBarAsk.style.width = `${100 - bidPct}%`;
        }

        // Render Asks (Render inverted: highest down to lowest/best ask)
        const reversedAsks = [...asks].reverse();
        if (reversedAsks.length === 0) {
            elements.asksContainer.innerHTML = '<div class="ob-empty-state">No active ask orders</div>';
        } else {
            let askRunningTotal = 0;
            elements.asksContainer.innerHTML = reversedAsks.map(ask => {
                askRunningTotal += ask.quantity;
                const widthPct = Math.min(100, Math.round((ask.quantity / maxVol) * 100));
                return `
                    <div class="ob-row ob-ask-row" onclick="window.exchangeApp.setPrice(${ask.price})">
                        <div class="ob-depth-bar" style="width: ${widthPct}%;"></div>
                        <span class="text-ask">${ask.price.toFixed(2)}</span>
                        <span class="text-right">${formatNumber(ask.quantity)}</span>
                        <span class="text-right text-muted">${formatNumber(ask.cumulativeQuantity || askRunningTotal)}</span>
                    </div>
                `;
            }).join('');
        }

        // Render Bids (Best Bid at top down to lowest)
        if (bids.length === 0) {
            elements.bidsContainer.innerHTML = '<div class="ob-empty-state">No active bid orders</div>';
        } else {
            let bidRunningTotal = 0;
            elements.bidsContainer.innerHTML = bids.map(bid => {
                bidRunningTotal += bid.quantity;
                const widthPct = Math.min(100, Math.round((bid.quantity / maxVol) * 100));
                return `
                    <div class="ob-row ob-bid-row" onclick="window.exchangeApp.setPrice(${bid.price})">
                        <div class="ob-depth-bar" style="width: ${widthPct}%;"></div>
                        <span class="text-bid">${bid.price.toFixed(2)}</span>
                        <span class="text-right">${formatNumber(bid.quantity)}</span>
                        <span class="text-right text-muted">${formatNumber(bid.cumulativeQuantity || bidRunningTotal)}</span>
                    </div>
                `;
            }).join('');
        }
    }

    function renderTrades(trades) {
        if (!trades || trades.length === 0) {
            elements.tradesFeedContainer.innerHTML = '<div class="ob-empty-state">No executed trades yet.</div>';
            return;
        }

        elements.tradesFeedContainer.innerHTML = trades.map(t => {
            const timeStr = t.timestamp ? new Date(t.timestamp).toLocaleTimeString() : '--:--:--';
            const turnover = (t.price * t.quantity);
            return `
                <div class="trade-row">
                    <span class="text-muted">${timeStr}</span>
                    <span class="text-bid">${formatCurrency(t.price)}</span>
                    <span class="text-right">${formatNumber(t.quantity)}</span>
                    <span class="text-right text-muted">${formatCurrency(turnover)}</span>
                </div>
            `;
        }).join('');
    }

    function renderOpenOrders(orders) {
        elements.openOrdersCount.textContent = orders.length;
        if (!orders || orders.length === 0) {
            elements.myOpenOrdersContainer.innerHTML = '<div class="ob-empty-state">No open resting orders.</div>';
            return;
        }

        elements.myOpenOrdersContainer.innerHTML = orders.map(o => {
            const sideClass = o.side === 'BUY' ? 'text-bid' : 'text-ask';
            return `
                <div class="open-order-card">
                    <div class="open-order-info">
                        <div class="open-order-main">
                            <span class="${sideClass}">${o.side}</span> ${formatNumber(o.quantity)} ${o.symbol} @ ${formatCurrency(o.price)}
                        </div>
                        <div class="open-order-sub">
                            Status: ${o.status} | Rem: ${formatNumber(o.remainingQuantity)} | ID: ${o.orderId.substring(0, 12)}
                        </div>
                    </div>
                    <button type="button" class="btn-cancel-order" onclick="window.exchangeApp.cancelOrder('${o.orderId}')">
                        ✕ Cancel
                    </button>
                </div>
            `;
        }).join('');
    }

    function renderPortfolio(port) {
        if (!port) return;

        elements.portUserFullname.textContent = `${port.fullName || port.username} (${port.username})`;
        elements.portCashVal.textContent = formatCurrency(port.cashBalance);
        elements.portHoldingsVal.textContent = formatCurrency(port.holdingsMarketValue);
        elements.portNetworthVal.textContent = formatCurrency(port.totalNetWorth);

        const pnl = port.totalPnL || 0;
        elements.portPnlVal.textContent = `${pnl >= 0 ? '+' : ''}${formatCurrency(pnl)}`;
        elements.portPnlVal.className = `metric-val ${pnl >= 0 ? 'text-bid' : 'text-ask'}`;

        // Update Ticket Available Resources hint
        if (state.side === 'BUY') {
            elements.availableHint.textContent = `Avail Cash: ${formatCurrency(port.cashBalance)}`;
        } else {
            const holding = (port.holdings || []).find(h => h.symbol === state.symbol);
            const sharesOwned = holding ? holding.quantity : 0;
            elements.availableHint.textContent = `Owned: ${formatNumber(sharesOwned)} sh`;
        }

        // Render Holdings Table
        const holdings = port.holdings || [];
        if (holdings.length === 0) {
            elements.holdingsTableBody.innerHTML = `
                <tr><td colspan="8" class="text-center text-muted" style="padding: 20px;">No open stock positions currently held.</td></tr>
            `;
            return;
        }

        elements.holdingsTableBody.innerHTML = holdings.map(h => {
            const retPct = h.returnPercent || 0;
            const retClass = retPct >= 0 ? 'text-bid' : 'text-ask';
            return `
                <tr>
                    <td style="font-weight: 700; color: var(--color-cyan);">${h.symbol}</td>
                    <td class="text-muted">${h.companyName}</td>
                    <td class="text-right">${formatNumber(h.quantity)}</td>
                    <td class="text-right">${formatCurrency(h.averageBuyPrice)}</td>
                    <td class="text-right">${formatCurrency(h.currentPrice)}</td>
                    <td class="text-right" style="font-weight: 600;">${formatCurrency(h.marketValue)}</td>
                    <td class="text-right ${retClass}">${h.unrealizedPnL >= 0 ? '+' : ''}${formatCurrency(h.unrealizedPnL)}</td>
                    <td class="text-right ${retClass}">${h.returnPercent >= 0 ? '+' : ''}${retPct.toFixed(2)}%</td>
                </tr>
            `;
        }).join('');
    }

    // =========================================================================
    // User Interactions & Order Submission
    // =========================================================================
    function updateOrderEstimate() {
        const qty = parseFloat(elements.orderQtyInput.value) || 0;
        const price = state.orderType === 'MARKET' ? (state.stocks[state.symbol]?.currentPrice || 0) : (parseFloat(elements.orderPriceInput.value) || 0);
        const total = qty * price;
        elements.orderEstTotal.textContent = formatCurrency(total);
    }

    async function handleOrderSubmit() {
        const qty = parseInt(elements.orderQtyInput.value, 10);
        const price = state.orderType === 'MARKET' ? 0.0 : parseFloat(elements.orderPriceInput.value);

        if (!qty || qty <= 0) {
            showOrderAlert('Please specify a positive integer share quantity.', 'danger');
            return;
        }

        if (state.orderType === 'LIMIT' && (!price || price <= 0)) {
            showOrderAlert('Please specify a valid limit price.', 'danger');
            return;
        }

        const payload = {
            symbol: state.symbol,
            side: state.side,
            type: state.orderType,
            price: price,
            quantity: qty
        };

        try {
            elements.submitOrderBtn.disabled = true;
            elements.submitOrderBtn.textContent = 'SUBMITTING TO ENGINE...';

            const result = await apiRequest('/api/orders', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            showOrderAlert(`Order ${result.orderId} submitted! Status: ${result.status}`, 'success');
            await refreshAllData();
        } catch (e) {
            showOrderAlert(`Rejection: ${e.message}`, 'danger');
        } finally {
            elements.submitOrderBtn.disabled = false;
            elements.submitOrderBtn.textContent = `PLACE ${state.side} ORDER`;
        }
    }

    async function cancelOrder(orderId) {
        try {
            await apiRequest(`/api/orders/${orderId}`, { method: 'DELETE' });
            showOrderAlert(`Order ${orderId.substring(0, 8)} cancelled successfully.`, 'success');
            await refreshAllData();
        } catch (e) {
            showOrderAlert(`Cancellation failed: ${e.message}`, 'danger');
        }
    }

    function showOrderAlert(msg, type = 'success') {
        elements.orderAlert.textContent = msg;
        elements.orderAlert.className = `order-alert alert-${type}`;
        elements.orderAlert.style.display = 'block';
        setTimeout(() => {
            elements.orderAlert.style.display = 'none';
        }, 5000);
    }

    // =========================================================================
    // Presentation Scripted Demo Runner (Slides 07 & 11)
    // =========================================================================
    async function triggerDemoScenario() {
        elements.demoModal.style.display = 'flex';
        elements.demoModalBody.innerHTML = `
            <div class="loading-spinner-box">
                <div class="spinner"></div>
                <p>Executing Presentation Demo Scenario against Matching Engine...</p>
            </div>
        `;

        try {
            const resp = await apiRequest('/api/demo/run-scenario', { method: 'POST' });
            renderDemoResults(resp);
            await refreshAllData();
        } catch (e) {
            elements.demoModalBody.innerHTML = `
                <div class="order-alert alert-danger">
                    <strong>Demo Execution Error:</strong> ${e.message}
                </div>
            `;
        }
    }

    function renderDemoResults(data) {
        const steps = data.steps || [];
        elements.demoModalBody.innerHTML = `
            <div class="demo-timeline">
                ${steps.map(s => `
                    <div class="demo-step-card">
                        <span class="step-badge">STEP ${s.step}: ${s.action}</span>
                        <div class="step-title">${s.trader ? s.trader + ' — ' : ''}${s.details || ''}</div>
                        ${s.makerPriceRule ? `
                            <div class="step-highlight">
                                <strong>Execution Rule:</strong> ${s.makerPriceRule} (${s.quantityMatched} shares @ $${s.executionPrice})
                            </div>
                        ` : ''}
                        ${s.aliceBalanceChange ? `
                            <div style="font-size: 0.76rem; color: var(--text-muted); margin-top: 4px;">
                                <div>Alice: ${s.aliceBalanceChange}</div>
                                <div>Bob: ${s.bobBalanceChange}</div>
                                <div>Resting in Book: <strong>${s.aliceRemainingOrderInBook}</strong></div>
                            </div>
                        ` : ''}
                    </div>
                `).join('')}
            </div>
        `;
    }

    // =========================================================================
    // Event Listeners & Binding
    // =========================================================================
    function setupEventListeners() {
        // Symbol selection
        elements.symbolTabs.addEventListener('click', (e) => {
            const btn = e.target.closest('.symbol-tab');
            if (!btn) return;
            const sym = btn.dataset.symbol;
            if (sym && sym !== state.symbol) {
                document.querySelectorAll('.symbol-tab').forEach(t => t.classList.remove('active'));
                btn.classList.add('active');
                state.symbol = sym;
                refreshAllData();
            }
        });

        // Trader selection
        elements.traderSelect.addEventListener('change', (e) => {
            state.trader = e.target.value;
            // Also attempt login to update session
            fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: state.trader, password: 'password123' })
            }).catch(() => {});
            refreshAllData();
        });

        // Side toggle
        elements.sideBuyBtn.addEventListener('click', () => {
            state.side = 'BUY';
            elements.sideBuyBtn.className = 'side-btn active-buy';
            elements.sideSellBtn.className = 'side-btn';
            elements.submitOrderBtn.className = 'btn btn-submit-order buy-mode';
            elements.submitOrderBtn.textContent = 'PLACE BUY ORDER';
            updateOrderEstimate();
            if (state.portfolio) renderPortfolio(state.portfolio);
        });

        elements.sideSellBtn.addEventListener('click', () => {
            state.side = 'SELL';
            elements.sideSellBtn.className = 'side-btn active-sell';
            elements.sideBuyBtn.className = 'side-btn';
            elements.submitOrderBtn.className = 'btn btn-submit-order sell-mode';
            elements.submitOrderBtn.textContent = 'PLACE SELL ORDER';
            updateOrderEstimate();
            if (state.portfolio) renderPortfolio(state.portfolio);
        });

        // Order Type select
        elements.orderTypeSelect.addEventListener('change', (e) => {
            state.orderType = e.target.value;
            if (state.orderType === 'MARKET') {
                elements.priceInputGroup.style.opacity = '0.5';
                elements.orderPriceInput.disabled = true;
            } else {
                elements.priceInputGroup.style.opacity = '1';
                elements.orderPriceInput.disabled = false;
            }
            updateOrderEstimate();
        });

        // Numerical input listeners
        elements.orderPriceInput.addEventListener('input', updateOrderEstimate);
        elements.orderQtyInput.addEventListener('input', updateOrderEstimate);

        // Quick fractional quantity buttons
        document.querySelectorAll('.quick-qty-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const fraction = parseFloat(btn.dataset.fraction) || 1.0;
                if (!state.portfolio) return;

                if (state.side === 'BUY') {
                    const price = parseFloat(elements.orderPriceInput.value) || 200.0;
                    const maxShares = Math.floor(state.portfolio.cashBalance / price);
                    elements.orderQtyInput.value = Math.max(1, Math.floor(maxShares * fraction));
                } else {
                    const holding = (state.portfolio.holdings || []).find(h => h.symbol === state.symbol);
                    const maxShares = holding ? holding.quantity : 0;
                    elements.orderQtyInput.value = Math.max(1, Math.floor(maxShares * fraction));
                }
                updateOrderEstimate();
            });
        });

        // Submit form
        document.getElementById('order-form').addEventListener('submit', (e) => {
            e.preventDefault();
            handleOrderSubmit();
        });

        // Panel tabs (Trades vs My Orders)
        elements.tabTradesFeed.addEventListener('click', () => {
            elements.tabTradesFeed.classList.add('active');
            elements.tabMyOrders.classList.remove('active');
            elements.contentTradesFeed.classList.add('active');
            elements.contentMyOrders.classList.remove('active');
        });

        elements.tabMyOrders.addEventListener('click', () => {
            elements.tabMyOrders.classList.add('active');
            elements.tabTradesFeed.classList.remove('active');
            elements.contentMyOrders.classList.add('active');
            elements.contentTradesFeed.classList.remove('active');
        });

        // Demo Modal triggers
        elements.runDemoBtn.addEventListener('click', triggerDemoScenario);
        elements.modalCloseBtn.addEventListener('click', () => elements.demoModal.style.display = 'none');
        elements.modalDismissBtn.addEventListener('click', () => elements.demoModal.style.display = 'none');
        elements.modalRerunBtn.addEventListener('click', triggerDemoScenario);
    }

    async function refreshAllData() {
        await Promise.allSettled([
            fetchStocks(),
            fetchOrderBook(),
            fetchTrades(),
            fetchPortfolio(),
            fetchOpenOrders()
        ]);
        updateOrderEstimate();
    }

    // Public API for inline onclick bindings
    window.exchangeApp = {
        setPrice: (price) => {
            if (state.orderType === 'LIMIT') {
                elements.orderPriceInput.value = Number(price).toFixed(2);
                updateOrderEstimate();
            }
        },
        cancelOrder: cancelOrder
    };

    // =========================================================================
    // Bootstrapping
    // =========================================================================
    async function init() {
        setupEventListeners();
        await refreshAllData();

        // 1-second real-time polling loop
        setInterval(() => {
            if (state.isPolling) {
                fetchOrderBook();
                fetchTrades();
                fetchPortfolio();
                fetchOpenOrders();
            }
        }, 1200);

        // 5-second slower ticker refresh
        setInterval(() => {
            if (state.isPolling) fetchStocks();
        }, 5000);
    }

    window.addEventListener('DOMContentLoaded', init);
})();
