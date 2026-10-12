/**
 * AURA EXCHANGE — INSTITUTIONAL MONOCHROMATIC TRADING CLIENT
 * Features:
 * - Real-time L2 Order Book depth rendering with cumulative size bars
 * - High-resolution vector SVG stock logos for all 11 market equities
 * - OLED Black / Swiss Minimalist White theme switcher with persistence
 * - 8 distinct trader account personas
 * - 1-click price copying, fraction sizing, and resting order cancellations
 * - Slide 07 & 11 presentation matching scenario visualizer
 */

(() => {
    // -------------------------------------------------------------------------
    // Vector SVG Stock Logos Catalog (Clean Monochromatic Vectors)
    // -------------------------------------------------------------------------
    const STOCK_LOGOS = {
        AAPL: `<svg viewBox="0 0 24 24"><path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 6.37c.56-.68.95-1.62.84-2.57-.82.03-1.82.55-2.4 1.23-.52.6-.97 1.56-.85 2.48.92.07 1.85-.46 2.41-1.14z"/></svg>`,
        MSFT: `<svg viewBox="0 0 24 24"><path d="M1 1h10v10H1V1zm12 0h10v10H13V1zM1 13h10v10H1V13zm12 0h10v10H13V13z"/></svg>`,
        GOOGL: `<svg viewBox="0 0 24 24"><path d="M12 2a10 10 0 1 0 7.07 17.07l-2.83-2.83A6 6 0 1 1 12 6c1.66 0 3.14.69 4.22 1.78l2.83-2.83A9.97 9.97 0 0 0 12 2zm1 8h7v4h-7v-4z"/></svg>`,
        TSLA: `<svg viewBox="0 0 24 24"><path d="M12 4.5c2.8 0 5.4.7 7.7 2l-1 2.2c-2-1.1-4.2-1.7-6.7-1.7-2.5 0-4.7.6-6.7 1.7l-1-2.2c2.3-1.3 4.9-2 7.7-2zm0 3.8c1.7 0 3.4.4 5 1.1l-.8 2c-1.3-.6-2.7-.9-4.2-.9-1.5 0-2.9.3-4.2.9l-.8-2c1.6-.7 3.3-1.1 5-1.1zm-1 4.7h2v7h-2v-7z"/></svg>`,
        NVDA: `<svg viewBox="0 0 24 24"><path d="M3.7 9.2c1.8-3.4 5-5.2 8.3-5.2 5 0 9 3.6 9 8 0 4.1-3.4 7.5-8 7.5-3.6 0-6.8-2.1-8-5.3 1.1 2.1 3.4 3.3 5.8 3.3 3.6 0 6.2-2.5 6.2-5.5 0-2.8-2.4-5-5.4-5-3.1 0-5.4 2-6.5 4.7zm3.1 2.8c.8-1.5 2.1-2.4 3.7-2.4 2 0 3.5 1.4 3.5 3.2 0 1.6-1.3 2.9-3.1 2.9-1.7 0-3.3-.9-4.1-2.5z"/></svg>`,
        AMZN: `<svg viewBox="0 0 24 24"><path d="M13.9 12.1c-.2.8-.7 1.4-1.5 1.4-.7 0-1.1-.5-1.1-1.3 0-1.6 1.3-2.2 2.6-2.2v2.1zm2.3 4.4c-.2-.3-.3-.8-.3-1.3-.6.9-1.5 1.4-2.6 1.4-1.6 0-2.8-1-2.8-2.6 0-2 1.6-3 4-3v-.3c0-.6-.3-1.1-1.2-1.1-.8 0-1.6.3-2.1.7l-.6-1.1c.8-.6 1.9-.9 3-.9 2.1 0 3 1.2 3 3.1v3.5c0 .7.1 1.2.3 1.6h-1.7zm5.2 2.3c-7.7 4.2-16.7 1.8-20.4-.8-.4-.3 0-.7.4-.5 4.5 2.4 13.8 3.5 19.3-.1.6-.4 1.1.8.7 1.4zm1.5-1.2c-.4-.5-2.6-.3-3.9-.2-.3 0-.4-.3-.1-.5 1.7-1.1 4.5-.8 4.8-.4.3.4-.1 3.2-1.7 4.5-.3.2-.5.1-.4-.2.4-1 1.7-2.7 1.3-3.2z"/></svg>`,
        META: `<svg viewBox="0 0 24 24"><path d="M16.9 5.2c-2.1 0-3.9 1.4-4.9 3-1-1.6-2.8-3-4.9-3C3.2 5.2.5 8.3.5 12.1c0 4.1 3 7.4 7.2 7.4 2.2 0 4.1-1.3 5.1-3 1 1.7 2.9 3 5.1 3 4.2 0 7.2-3.3 7.2-7.4 0-3.8-2.7-6.9-6.2-6.9zm-9.8 12c-2.8 0-4.8-2.2-4.8-5.1 0-2.6 1.8-4.8 4.3-4.8 1.9 0 3.5 1.5 4.3 3.4-.9 3.5-2.3 6.5-3.8 6.5zm9.8 0c-1.5 0-2.9-3-3.8-6.5.8-1.9 2.4-3.4 4.3-3.4 2.5 0 4.3 2.2 4.3 4.8 0 2.9-2 5.1-4.8 5.1z"/></svg>`,
        AMD: `<svg viewBox="0 0 24 24"><path d="M1.5 1.5h14.2l-3.5 3.5H5v7.2L1.5 8.7V1.5zm21 0v14.2l-3.5-3.5V5h-7.2L15.3 1.5H22.5zm0 21H8.3l3.5-3.5H19v-7.2l3.5 3.5v7.2zM5.3 8.7l3.4 3.4v6.8H1.9L5.3 8.7z"/></svg>`,
        PLTR: `<svg viewBox="0 0 24 24"><path d="M12 2a8 8 0 0 0-5.8 13.5l-3.7 3.7 1.4 1.4 3.7-3.7A8 8 0 1 0 12 2zm0 14a6 6 0 1 1 0-12 6 6 0 0 1 0 12zm0-9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z"/></svg>`,
        COIN: `<svg viewBox="0 0 24 24"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm0 16a6 6 0 1 1 0-12 6 6 0 0 1 0 12zm0-9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z"/></svg>`,
        JPM: `<svg viewBox="0 0 24 24"><path d="M7.4 2h9.2l5.4 5.4v9.2l-5.4 5.4H7.4L2 16.6V7.4L7.4 2zm1.2 2.5L4.5 8.6v6.8l4.1 4.1h6.8l4.1-4.1V8.6l-4.1-4.1H8.6z"/></svg>`
    };

    // Trader Initials Map
    const TRADER_AVATARS = {
        alice: 'AS',
        bob: 'BJ',
        charlie: 'CB',
        diana: 'DP',
        edward: 'EN',
        fiona: 'FG',
        hedge_fund: 'AC',
        market_maker: 'CS'
    };

    // Application Reactive State
    const state = {
        symbol: 'AAPL',
        trader: 'alice',
        side: 'BUY',
        orderType: 'LIMIT',
        stocks: {},
        portfolio: null,
        theme: localStorage.getItem('aura_theme') || 'dark',
        isPolling: true,
        mobileView: 'trade'
    };

    // DOM Elements Cache
    const el = {
        html: document.documentElement,
        workspace: document.getElementById('terminal-workspace'),
        mobileViewNav: document.getElementById('mobile-view-nav'),
        mobileNavBtns: document.querySelectorAll('.mobile-nav-btn'),
        mNavOrdersCount: document.getElementById('m-nav-orders-count'),
        themeBtn: document.getElementById('theme-toggle-btn'),
        themeIcon: document.getElementById('theme-icon'),
        themeLabel: document.getElementById('theme-label'),
        traderSelect: document.getElementById('trader-select'),
        traderAvatar: document.getElementById('trader-avatar'),
        runDemoBtn: document.getElementById('run-demo-btn'),
        tickerTrack: document.getElementById('ticker-scroll-track'),

        // Order Book
        obSymbolTag: document.getElementById('ob-symbol-tag'),
        obTotalDepth: document.getElementById('ob-total-depth'),
        asksContainer: document.getElementById('orderbook-asks-container'),
        bidsContainer: document.getElementById('orderbook-bids-container'),
        spreadValue: document.getElementById('spread-value'),
        spreadMid: document.getElementById('spread-mid'),
        depthRatioBid: document.getElementById('depth-ratio-bid'),
        depthRatioAsk: document.getElementById('depth-ratio-ask'),
        depthTextBids: document.getElementById('depth-text-bids'),
        depthTextAsks: document.getElementById('depth-text-asks'),

        // Hero
        heroLogoFrame: document.getElementById('hero-logo-frame'),
        heroSymbol: document.getElementById('hero-symbol'),
        heroCompany: document.getElementById('hero-company'),
        heroLastPrice: document.getElementById('hero-last-price'),
        heroChangePill: document.getElementById('hero-change-pill'),
        statBestBid: document.getElementById('stat-best-bid'),
        statBestAsk: document.getElementById('stat-best-ask'),
        statPrevClose: document.getElementById('stat-prev-close'),
        statVolume: document.getElementById('stat-volume'),

        // Order Ticket
        sideBuyBtn: document.getElementById('side-buy-btn'),
        sideSellBtn: document.getElementById('side-sell-btn'),
        orderTypeSelect: document.getElementById('order-type-select'),
        priceInputRow: document.getElementById('price-input-row'),
        orderPriceInput: document.getElementById('order-price-input'),
        orderQtyInput: document.getElementById('order-qty-input'),
        availResourceText: document.getElementById('avail-resource-text'),
        orderEstNotional: document.getElementById('order-est-notional'),
        submitOrderBtn: document.getElementById('submit-order-btn'),
        orderStatusMsg: document.getElementById('order-status-msg'),

        // Activity Tabs
        tabTapeBtn: document.getElementById('tab-tape-btn'),
        tabOrdersBtn: document.getElementById('tab-orders-btn'),
        bodyTradeTape: document.getElementById('body-trade-tape'),
        bodyOpenOrders: document.getElementById('body-open-orders'),
        tradesTapeList: document.getElementById('trades-tape-list'),
        openOrdersList: document.getElementById('open-orders-list'),
        openOrdersBadge: document.getElementById('open-orders-badge'),

        // Portfolio
        portAccountName: document.getElementById('port-account-name'),
        kpiCash: document.getElementById('kpi-cash'),
        kpiEquity: document.getElementById('kpi-equity'),
        kpiNetworth: document.getElementById('kpi-networth'),
        kpiPnl: document.getElementById('kpi-pnl'),
        holdingsTbody: document.getElementById('holdings-table-tbody'),

        // Modal
        demoModal: document.getElementById('demo-modal'),
        demoModalContent: document.getElementById('demo-modal-content'),
        modalCloseX: document.getElementById('modal-close-x'),
        modalCloseAction: document.getElementById('modal-close-action'),
        modalReplayAction: document.getElementById('modal-replay-action')
    };

    // -------------------------------------------------------------------------
    // Utilities & Formatters
    // -------------------------------------------------------------------------
    const fmtMoney = (n) => '$' + (Number(n) || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const fmtNumber = (n) => (Number(n) || 0).toLocaleString('en-US');

    // -------------------------------------------------------------------------
    // Theme Management (OLED Black vs Swiss White)
    // -------------------------------------------------------------------------
    function applyTheme(themeName) {
        state.theme = themeName;
        el.html.dataset.theme = themeName;
        localStorage.setItem('aura_theme', themeName);

        if (themeName === 'light') {
            el.themeIcon.textContent = '○';
            el.themeLabel.textContent = 'WHITE';
        } else {
            el.themeIcon.textContent = '◐';
            el.themeLabel.textContent = 'OLED';
        }
    }

    function toggleTheme() {
        const nextTheme = state.theme === 'dark' ? 'light' : 'dark';
        applyTheme(nextTheme);
    }

    // -------------------------------------------------------------------------
    // Mobile Viewport Navigation
    // -------------------------------------------------------------------------
    function setMobileView(view) {
        state.mobileView = view;
        if (el.workspace) {
            el.workspace.setAttribute('data-active-view', view);
        }
        if (el.mobileNavBtns) {
            el.mobileNavBtns.forEach(btn => {
                btn.classList.toggle('active', btn.dataset.view === view);
            });
        }
    }

    // -------------------------------------------------------------------------
    // API Communication Client
    // -------------------------------------------------------------------------
    async function api(path, opts = {}) {
        const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
        if (state.trader) {
            headers['X-User-Id'] = state.trader;
        }
        const res = await fetch(path, { ...opts, headers });
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: res.statusText }));
            throw new Error(err.error || err.message || 'API Communication Failure');
        }
        return res.json();
    }

    // -------------------------------------------------------------------------
    // Data Ingestion & State Refreshers
    // -------------------------------------------------------------------------
    async function refreshStocks() {
        try {
            const stockList = await api('/api/stocks');
            stockList.forEach(s => {
                state.stocks[s.symbol] = s;
            });
            renderTickerStrip(stockList);
            updateHeroHeader();
        } catch (e) {
            console.warn('Stocks ingestion error:', e.message);
        }
    }

    async function refreshOrderBook() {
        try {
            const book = await api(`/api/orderbook?symbol=${state.symbol}&depth=15`);
            renderOrderBook(book);
        } catch (e) {
            console.warn('Order book ingestion error:', e.message);
        }
    }

    async function refreshTrades() {
        try {
            const trades = await api(`/api/trades?symbol=${state.symbol}&limit=30`);
            renderTradesTape(trades);
        } catch (e) {
            console.warn('Trades ingestion error:', e.message);
        }
    }

    async function refreshPortfolio() {
        try {
            const p = await api('/api/portfolio');
            state.portfolio = p;
            renderPortfolio(p);
        } catch (e) {
            console.warn('Portfolio ingestion error:', e.message);
        }
    }

    async function refreshOpenOrders() {
        try {
            const orders = await api('/api/orders?activeOnly=true');
            renderOpenOrders(orders);
        } catch (e) {
            console.warn('Open orders ingestion error:', e.message);
        }
    }

    // -------------------------------------------------------------------------
    // UI Renderers
    // -------------------------------------------------------------------------
    function renderTickerStrip(stocks) {
        if (!stocks || stocks.length === 0) return;

        el.tickerTrack.innerHTML = stocks.map(s => {
            const isActive = s.symbol === state.symbol;
            const chg = s.changePercent || 0;
            const diffClass = chg >= 0 ? 'text-bid' : 'text-ask';
            const diffSign = chg >= 0 ? '+' : '';
            const logoSvg = STOCK_LOGOS[s.symbol] || STOCK_LOGOS.AAPL;

            return `
                <button type="button" class="ticker-tab-btn ${isActive ? 'active' : ''}" data-symbol="${s.symbol}">
                    <span class="tab-logo-icon">${logoSvg}</span>
                    <span class="tab-sym">${s.symbol}</span>
                    <span class="tab-val num-font">${fmtMoney(s.currentPrice)}</span>
                    <span class="tab-diff num-font ${diffClass}">${diffSign}${chg.toFixed(2)}%</span>
                </button>
            `;
        }).join('');
    }

    function updateHeroHeader() {
        const s = state.stocks[state.symbol];
        if (!s) return;

        el.heroLogoFrame.innerHTML = STOCK_LOGOS[s.symbol] || STOCK_LOGOS.AAPL;
        el.heroSymbol.textContent = s.symbol;
        el.heroCompany.textContent = s.companyName;
        el.statPrevClose.textContent = fmtMoney(s.previousClose || (s.currentPrice * 0.99));
        el.statVolume.textContent = fmtNumber(s.totalVolume || 100000);

        const chg = s.changePercent || 0;
        el.heroChangePill.textContent = `${chg >= 0 ? '+' : ''}${chg.toFixed(2)}%`;
        el.heroChangePill.className = `hero-change-pill num-font ${chg >= 0 ? 'text-bid' : 'text-ask'}`;
    }

    function renderOrderBook(book) {
        el.obSymbolTag.textContent = state.symbol;
        if (!book) return;

        const bids = book.bids || [];
        const asks = book.asks || [];

        const bestBid = bids.length > 0 ? bids[0].price : null;
        const bestAsk = asks.length > 0 ? asks[0].price : null;

        el.statBestBid.textContent = bestBid ? fmtMoney(bestBid) : '---';
        el.statBestAsk.textContent = bestAsk ? fmtMoney(bestAsk) : '---';
        if (book.lastPrice && book.lastPrice > 0) {
            el.heroLastPrice.textContent = fmtMoney(book.lastPrice);
        } else if (bestBid && bestAsk) {
            el.heroLastPrice.textContent = fmtMoney((bestBid + bestAsk) / 2);
        }

        // Spread & Mid Calculation
        if (bestBid && bestAsk) {
            const spread = bestAsk - bestBid;
            const spreadPct = (spread / bestAsk) * 100;
            const mid = (bestBid + bestAsk) / 2;
            el.spreadValue.textContent = `${fmtMoney(spread)} (${spreadPct.toFixed(2)}%)`;
            el.spreadMid.textContent = fmtMoney(mid);
        } else {
            el.spreadValue.textContent = 'NO SPREAD';
            el.spreadMid.textContent = '---';
        }

        // Depth Ratios
        const totalBidVol = bids.reduce((acc, b) => acc + b.quantity, 0);
        const totalAskVol = asks.reduce((acc, a) => acc + a.quantity, 0);
        const totalDepth = totalBidVol + totalAskVol;
        el.obTotalDepth.textContent = `DEPTH: ${fmtNumber(totalDepth)}`;

        if (totalDepth > 0) {
            const bidPct = Math.round((totalBidVol / totalDepth) * 100);
            el.depthRatioBid.style.width = `${bidPct}%`;
            el.depthRatioAsk.style.width = `${100 - bidPct}%`;
            el.depthTextBids.textContent = `BID: ${bidPct}% (${fmtNumber(totalBidVol)})`;
            el.depthTextAsks.textContent = `ASK: ${100 - bidPct}% (${fmtNumber(totalAskVol)})`;
        }

        const maxVol = Math.max(totalBidVol, totalAskVol, 1);

        // Render Asks (Inverted order: highest to lowest down to best ask)
        const reversedAsks = [...asks].reverse();
        if (reversedAsks.length === 0) {
            el.asksContainer.innerHTML = '<div class="book-placeholder">No active asks</div>';
        } else {
            let running = 0;
            el.asksContainer.innerHTML = reversedAsks.map(a => {
                running += a.quantity;
                const barWidth = Math.min(100, Math.round((a.quantity / maxVol) * 100));
                return `
                    <div class="ladder-row ask-row" onclick="window.exchange.copyPrice(${a.price})">
                        <div class="depth-bar-fill" style="width: ${barWidth}%;"></div>
                        <span class="text-ask num-font">${a.price.toFixed(2)}</span>
                        <span class="text-right num-font">${fmtNumber(a.quantity)}</span>
                        <span class="text-right num-font text-muted">${fmtNumber(a.cumulativeQuantity || running)}</span>
                    </div>
                `;
            }).join('');
        }

        // Render Bids (Best Bid at top down to lowest)
        if (bids.length === 0) {
            el.bidsContainer.innerHTML = '<div class="book-placeholder">No active bids</div>';
        } else {
            let running = 0;
            el.bidsContainer.innerHTML = bids.map(b => {
                running += b.quantity;
                const barWidth = Math.min(100, Math.round((b.quantity / maxVol) * 100));
                return `
                    <div class="ladder-row bid-row" onclick="window.exchange.copyPrice(${b.price})">
                        <div class="depth-bar-fill" style="width: ${barWidth}%;"></div>
                        <span class="text-bid num-font">${b.price.toFixed(2)}</span>
                        <span class="text-right num-font">${fmtNumber(b.quantity)}</span>
                        <span class="text-right num-font text-muted">${fmtNumber(b.cumulativeQuantity || running)}</span>
                    </div>
                `;
            }).join('');
        }
    }

    function renderTradesTape(trades) {
        if (!trades || trades.length === 0) {
            el.tradesTapeList.innerHTML = '<div class="book-placeholder">No executed trades yet.</div>';
            return;
        }

        el.tradesTapeList.innerHTML = trades.map(t => {
            const time = t.timestamp ? new Date(t.timestamp).toLocaleTimeString() : '--:--:--';
            const notional = t.price * t.quantity;
            return `
                <div class="tape-row">
                    <span class="text-muted num-font">${time}</span>
                    <span class="text-bid num-font">${fmtMoney(t.price)}</span>
                    <span class="text-right num-font">${fmtNumber(t.quantity)}</span>
                    <span class="text-right num-font text-muted">${fmtMoney(notional)}</span>
                </div>
            `;
        }).join('');
    }

    function renderOpenOrders(orders) {
        el.openOrdersBadge.textContent = orders.length;
        if (el.mNavOrdersCount) {
            el.mNavOrdersCount.textContent = orders.length;
        }
        if (!orders || orders.length === 0) {
            el.openOrdersList.innerHTML = '<div class="book-placeholder">No resting orders for this account.</div>';
            return;
        }

        el.openOrdersList.innerHTML = orders.map(o => {
            const sideClass = o.side === 'BUY' ? 'text-bid' : 'text-ask';
            return `
                <div class="order-item-row">
                    <div class="order-item-desc">
                        <div class="order-item-main">
                            <span class="${sideClass}">${o.side}</span> ${fmtNumber(o.quantity)} ${o.symbol} @ ${fmtMoney(o.price)}
                        </div>
                        <div class="order-item-sub num-font">
                            ${o.status} | Rem: ${fmtNumber(o.remainingQuantity)} | #${o.orderId.substring(0, 8)}
                        </div>
                    </div>
                    <button type="button" class="cancel-action-btn" onclick="window.exchange.cancelOrder('${o.orderId}')">
                        CANCEL
                    </button>
                </div>
            `;
        }).join('');
    }

    function renderPortfolio(port) {
        if (!port) return;

        el.portAccountName.textContent = port.fullName || port.username;
        el.kpiCash.textContent = fmtMoney(port.cashBalance);
        el.kpiEquity.textContent = fmtMoney(port.holdingsMarketValue);
        el.kpiNetworth.textContent = fmtMoney(port.totalNetWorth);

        const pnl = port.totalPnL || 0;
        el.kpiPnl.textContent = `${pnl >= 0 ? '+' : ''}${fmtMoney(pnl)}`;
        el.kpiPnl.className = `kpi-v num-font ${pnl >= 0 ? 'text-bid' : 'text-ask'}`;

        // Available Resources calculation in Ticket
        if (state.side === 'BUY') {
            el.availResourceText.textContent = `Avail: ${fmtMoney(port.cashBalance)}`;
        } else {
            const hold = (port.holdings || []).find(h => h.symbol === state.symbol);
            const qty = hold ? hold.quantity : 0;
            el.availResourceText.textContent = `Owned: ${fmtNumber(qty)} sh`;
        }

        // Holdings Table
        const holdings = port.holdings || [];
        if (holdings.length === 0) {
            el.holdingsTbody.innerHTML = `
                <tr><td colspan="8" class="text-center text-muted" style="padding: 24px;">No open positions in portfolio.</td></tr>
            `;
            return;
        }

        el.holdingsTbody.innerHTML = holdings.map(h => {
            const ret = h.returnPercent || 0;
            const retClass = ret >= 0 ? 'text-bid' : 'text-ask';
            const logoSvg = STOCK_LOGOS[h.symbol] || STOCK_LOGOS.AAPL;

            return `
                <tr>
                    <td style="font-weight: 700;">
                        <span style="display: inline-flex; align-items: center; gap: 6px;">
                            <span style="width: 14px; height: 14px; display: inline-flex;">${logoSvg}</span>
                            ${h.symbol}
                        </span>
                    </td>
                    <td class="text-muted">${h.companyName}</td>
                    <td class="text-right num-font">${fmtNumber(h.quantity)}</td>
                    <td class="text-right num-font">${fmtMoney(h.averageBuyPrice)}</td>
                    <td class="text-right num-font">${fmtMoney(h.currentPrice)}</td>
                    <td class="text-right num-font" style="font-weight: 600;">${fmtMoney(h.marketValue)}</td>
                    <td class="text-right num-font ${retClass}">${h.unrealizedPnL >= 0 ? '+' : ''}${fmtMoney(h.unrealizedPnL)}</td>
                    <td class="text-right num-font ${retClass}">${h.returnPercent >= 0 ? '+' : ''}${ret.toFixed(2)}%</td>
                </tr>
            `;
        }).join('');
    }

    // -------------------------------------------------------------------------
    // Order Calculation & Execution
    // -------------------------------------------------------------------------
    function recalcNotional() {
        const qty = parseFloat(el.orderQtyInput.value) || 0;
        const price = state.orderType === 'MARKET' ? (state.stocks[state.symbol]?.currentPrice || 0) : (parseFloat(el.orderPriceInput.value) || 0);
        el.orderEstNotional.textContent = fmtMoney(qty * price);
    }

    async function handleOrderSubmission() {
        const qty = parseInt(el.orderQtyInput.value, 10);
        const price = state.orderType === 'MARKET' ? 0.0 : parseFloat(el.orderPriceInput.value);

        if (!qty || qty <= 0) {
            flashAlert('Specify a positive whole share quantity.', 'err');
            return;
        }

        if (state.orderType === 'LIMIT' && (!price || price <= 0)) {
            flashAlert('Enter a valid limit price.', 'err');
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
            el.submitOrderBtn.disabled = true;
            el.submitOrderBtn.textContent = 'TRANSMITTING ORDER...';

            const res = await api('/api/orders', {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            flashAlert(`Order #${res.orderId.substring(0, 8)} executed: ${res.status}`, 'ok');
            await syncAll();
        } catch (e) {
            flashAlert(e.message, 'err');
        } finally {
            el.submitOrderBtn.disabled = false;
            el.submitOrderBtn.textContent = `EXECUTE ${state.side} ORDER`;
        }
    }

    async function cancelOrder(orderId) {
        try {
            await api(`/api/orders/${orderId}`, { method: 'DELETE' });
            flashAlert(`Order #${orderId.substring(0, 8)} cancelled.`, 'ok');
            await syncAll();
        } catch (e) {
            flashAlert(e.message, 'err');
        }
    }

    function flashAlert(text, type = 'ok') {
        el.orderStatusMsg.textContent = text;
        el.orderStatusMsg.className = `ticket-status-msg msg-${type}`;
        el.orderStatusMsg.style.display = 'block';
        setTimeout(() => {
            el.orderStatusMsg.style.display = 'none';
        }, 5000);
    }

    // -------------------------------------------------------------------------
    // Scripted Presentation Scenario (Slides 07 & 11)
    // -------------------------------------------------------------------------
    async function executeDemoScenario() {
        el.demoModal.style.display = 'flex';
        el.demoModalContent.innerHTML = `
            <div class="modal-loader">
                <div class="monochrome-spinner"></div>
                <p>Executing Presentation Demo Scenario against Matching Engine...</p>
            </div>
        `;

        try {
            const data = await api('/api/demo/run-scenario', { method: 'POST' });
            renderDemoBreakdown(data);
            await syncAll();
        } catch (e) {
            el.demoModalContent.innerHTML = `
                <div class="ticket-status-msg msg-err">
                    <strong>Execution Error:</strong> ${e.message}
                </div>
            `;
        }
    }

    function renderDemoBreakdown(data) {
        const steps = data.steps || [];
        el.demoModalContent.innerHTML = `
            <div class="demo-breakdown-list">
                ${steps.map(s => `
                    <div class="demo-step-tile">
                        <span class="step-tag-pill">STEP ${s.step}: ${s.action.toUpperCase()}</span>
                        <div class="step-headline">${s.trader ? s.trader + ' — ' : ''}${s.details || ''}</div>
                        ${s.makerPriceRule ? `
                            <div class="step-rule-box">
                                <strong>Execution:</strong> ${s.makerPriceRule} (${s.quantityMatched} sh @ $${s.executionPrice})
                            </div>
                        ` : ''}
                        ${s.aliceBalanceChange ? `
                            <div style="font-size: 0.72rem; color: var(--text-muted); margin-top: 4px; font-family: 'JetBrains Mono', monospace;">
                                <div>Alice: ${s.aliceBalanceChange}</div>
                                <div>Bob: ${s.bobBalanceChange}</div>
                                <div>Resting Book Depth: <strong>${s.aliceRemainingOrderInBook}</strong></div>
                            </div>
                        ` : ''}
                    </div>
                `).join('')}
            </div>
        `;
    }

    // -------------------------------------------------------------------------
    // Event Wiring
    // -------------------------------------------------------------------------
    function initListeners() {
        // Theme Toggle
        el.themeBtn.addEventListener('click', toggleTheme);

        // Mobile Segmented View Switcher Listeners
        if (el.mobileNavBtns) {
            el.mobileNavBtns.forEach(btn => {
                btn.addEventListener('click', () => {
                    setMobileView(btn.dataset.view);
                });
            });
        }

        // Equity Ticker Selection
        el.tickerTrack.addEventListener('click', (e) => {
            const btn = e.target.closest('.ticker-tab-btn');
            if (!btn) return;
            const sym = btn.dataset.symbol;
            if (sym && sym !== state.symbol) {
                state.symbol = sym;
                document.querySelectorAll('.ticker-tab-btn').forEach(b => b.classList.toggle('active', b.dataset.symbol === sym));
                btn.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
                updateHeroHeader();
                syncAll();
            }
        });

        // Trader Selector
        el.traderSelect.addEventListener('change', (e) => {
            state.trader = e.target.value;
            el.traderAvatar.textContent = TRADER_AVATARS[state.trader] || 'TR';
            syncAll();
        });

        // Buy / Sell Selector
        el.sideBuyBtn.addEventListener('click', () => {
            state.side = 'BUY';
            el.sideBuyBtn.className = 'side-tab active-buy';
            el.sideSellBtn.className = 'side-tab';
            el.submitOrderBtn.className = 'submit-order-button btn-buy';
            el.submitOrderBtn.textContent = 'EXECUTE BUY ORDER';
            recalcNotional();
            if (state.portfolio) renderPortfolio(state.portfolio);
        });

        el.sideSellBtn.addEventListener('click', () => {
            state.side = 'SELL';
            el.sideSellBtn.className = 'side-tab active-sell';
            el.sideBuyBtn.className = 'side-tab';
            el.submitOrderBtn.className = 'submit-order-button btn-sell';
            el.submitOrderBtn.textContent = 'EXECUTE SELL ORDER';
            recalcNotional();
            if (state.portfolio) renderPortfolio(state.portfolio);
        });

        // Order Type Selector
        el.orderTypeSelect.addEventListener('change', (e) => {
            state.orderType = e.target.value;
            if (state.orderType === 'MARKET') {
                el.priceInputRow.style.opacity = '0.4';
                el.orderPriceInput.disabled = true;
            } else {
                el.priceInputRow.style.opacity = '1';
                el.orderPriceInput.disabled = false;
            }
            recalcNotional();
        });

        // Numerical input events
        el.orderPriceInput.addEventListener('input', recalcNotional);
        el.orderQtyInput.addEventListener('input', recalcNotional);

        // Fractional Sizing Buttons
        document.querySelectorAll('.size-pill').forEach(btn => {
            btn.addEventListener('click', () => {
                const fraction = parseFloat(btn.dataset.pct) || 1.0;
                if (!state.portfolio) return;

                if (state.side === 'BUY') {
                    const price = parseFloat(el.orderPriceInput.value) || 200.0;
                    const maxShares = Math.floor(state.portfolio.cashBalance / price);
                    el.orderQtyInput.value = Math.max(1, Math.floor(maxShares * fraction));
                } else {
                    const hold = (state.portfolio.holdings || []).find(h => h.symbol === state.symbol);
                    const maxShares = hold ? hold.quantity : 0;
                    el.orderQtyInput.value = Math.max(1, Math.floor(maxShares * fraction));
                }
                recalcNotional();
            });
        });

        // Submit form
        document.getElementById('order-form').addEventListener('submit', (e) => {
            e.preventDefault();
            handleOrderSubmission();
        });

        // Right Tabs (Tape vs Orders)
        el.tabTapeBtn.addEventListener('click', () => {
            el.tabTapeBtn.classList.add('active');
            el.tabOrdersBtn.classList.remove('active');
            el.bodyTradeTape.classList.add('active');
            el.bodyOpenOrders.classList.remove('active');
        });

        el.tabOrdersBtn.addEventListener('click', () => {
            el.tabOrdersBtn.classList.add('active');
            el.tabTapeBtn.classList.remove('active');
            el.bodyOpenOrders.classList.add('active');
            el.bodyTradeTape.classList.remove('active');
        });

        // Demo Modal Triggers
        el.runDemoBtn.addEventListener('click', executeDemoScenario);
        el.modalCloseX.addEventListener('click', () => el.demoModal.style.display = 'none');
        el.modalCloseAction.addEventListener('click', () => el.demoModal.style.display = 'none');
        el.modalReplayAction.addEventListener('click', executeDemoScenario);
    }

    async function syncAll() {
        await Promise.allSettled([
            refreshStocks(),
            refreshOrderBook(),
            refreshTrades(),
            refreshPortfolio(),
            refreshOpenOrders()
        ]);
        recalcNotional();
    }

    // Public window helper for inline callbacks
    window.exchange = {
        copyPrice: (price) => {
            if (state.orderType === 'LIMIT') {
                el.orderPriceInput.value = Number(price).toFixed(2);
                recalcNotional();
                if (window.innerWidth <= 860) {
                    setMobileView('trade');
                    el.orderPriceInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    el.orderPriceInput.focus();
                }
            }
        },
        cancelOrder: cancelOrder,
        setView: setMobileView
    };

    // -------------------------------------------------------------------------
    // Initialization Loop
    // -------------------------------------------------------------------------
    async function init() {
        applyTheme(state.theme);
        setMobileView('trade');
        initListeners();
        await syncAll();

        // 1-second high-density market polling loop
        setInterval(() => {
            if (state.isPolling) {
                refreshOrderBook();
                refreshTrades();
                refreshPortfolio();
                refreshOpenOrders();
            }
        }, 1200);

        // 6-second ticker strip refresh
        setInterval(() => {
            if (state.isPolling) refreshStocks();
        }, 6000);
    }

    window.addEventListener('DOMContentLoaded', init);
})();
