if (!window.__priceHawkContentReady) {
    window.__priceHawkContentReady = true;

    chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
        if (request.action === "extract") {
            const data = extractData();
            sendResponse(data);
            chrome.runtime.sendMessage({ action: "sendToBackend", data });
            return true;
        }

        if (request.action === "togglePriceHawkPanel") {
            togglePriceHawkPanel();
            sendResponse({ success: true });
            return true;
        }

        return false;
    });
}

function togglePriceHawkPanel() {
    const state = ensurePriceHawkPanel();
    const panel = state.shadow.getElementById("pricehawk-panel");

    if (panel.classList.contains("is-open")) {
        minimizePriceHawkPanel();
        return;
    }

    refreshPriceHawkProduct();
    openPriceHawkPanel();
}

function ensurePriceHawkPanel() {
    if (window.__priceHawkPanelState?.host?.isConnected) {
        return window.__priceHawkPanelState;
    }

    const host = document.createElement("div");
    host.id = "pricehawk-page-root";
    const shadow = host.attachShadow({ mode: "open" });

    shadow.innerHTML = `
        <style>
            :host {
                all: initial;
                color-scheme: light;
                font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
            }

            * {
                box-sizing: border-box;
            }

            .panel {
                position: fixed;
                top: 0;
                right: 0;
                z-index: 2147483647;
                display: flex;
                flex-direction: column;
                width: 25vw;
                min-width: 320px;
                max-width: 480px;
                height: 100vh;
                overflow: hidden;
                border: 1px solid rgba(255, 255, 255, 0.72);
                border-right: 0;
                border-radius: 20px 0 0 20px;
                background: linear-gradient(155deg, rgba(250, 252, 255, 0.94), rgba(238, 243, 250, 0.9));
                color: #172033;
                box-shadow: -20px 0 64px rgba(25, 39, 62, 0.18);
                backdrop-filter: blur(28px) saturate(145%);
                -webkit-backdrop-filter: blur(28px) saturate(145%);
                transform: translateX(110%);
                transition: transform 220ms cubic-bezier(0.2, 0.75, 0.25, 1);
            }

            .panel.is-open {
                transform: translateX(0);
            }

            .topbar {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                min-height: 72px;
                padding: 16px 20px;
                background: rgba(255, 255, 255, 0.48);
                border-bottom: 1px solid rgba(203, 213, 225, 0.65);
            }

            .brand {
                color: #1c2b43;
                font-size: 17px;
                font-weight: 750;
                letter-spacing: -0.035em;
                line-height: 1.1;
            }

            .platform {
                margin-top: 5px;
                color: #78859a;
                font-size: 10px;
                font-weight: 700;
                letter-spacing: 0.09em;
                text-transform: uppercase;
            }

            .icon-btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 32px;
                height: 32px;
                flex: 0 0 32px;
                border: 1px solid rgba(203, 213, 225, 0.86);
                border-radius: 10px;
                background: rgba(255, 255, 255, 0.72);
                color: #526078;
                cursor: pointer;
                font-size: 18px;
                line-height: 1;
                transition: background 150ms ease, border-color 150ms ease;
            }

            .icon-btn:hover {
                border-color: #aab6c8;
                background: #ffffff;
            }

            .content {
                flex: 1;
                min-height: 0;
                overflow: auto;
                padding: 20px;
                scrollbar-color: rgba(120, 133, 154, 0.4) transparent;
                scrollbar-width: thin;
            }

            .product {
                padding: 18px;
                border: 1px solid rgba(255, 255, 255, 0.88);
                border-radius: 15px;
                background: rgba(255, 255, 255, 0.66);
                box-shadow: 0 8px 24px rgba(36, 52, 80, 0.055);
            }

            .product-name {
                color: #26354d;
                font-size: 14px;
                font-weight: 650;
                line-height: 1.5;
                overflow-wrap: anywhere;
            }

            .meta-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                margin-top: 18px;
            }

            .badge {
                display: inline-flex;
                align-items: center;
                min-height: 25px;
                max-width: 45%;
                padding: 4px 8px;
                border-radius: 999px;
                background: rgba(226, 235, 248, 0.82);
                color: #4b6385;
                font-size: 10px;
                font-weight: 700;
                letter-spacing: 0.06em;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                text-transform: uppercase;
            }

            .price {
                color: #254c43;
                font-size: 23px;
                font-weight: 750;
                letter-spacing: -0.045em;
                line-height: 1.1;
                text-align: right;
                overflow-wrap: anywhere;
            }

            .actions {
                display: grid;
                gap: 8px;
                margin-top: 14px;
            }

            .btn {
                width: 100%;
                min-height: 41px;
                border: 1px solid rgba(203, 213, 225, 0.84);
                border-radius: 10px;
                cursor: pointer;
                font-family: inherit;
                font-size: 12px;
                font-weight: 650;
                transition: transform 150ms ease, background 150ms ease, border-color 150ms ease;
            }

            .btn:disabled {
                cursor: not-allowed;
                opacity: 0.55;
            }

            .btn:not(:disabled):active {
                transform: scale(0.99);
            }

            .btn-primary {
                border-color: #26364f;
                background: #26364f;
                color: #ffffff;
            }

            .btn-primary:hover:not(:disabled) {
                background: #354a68;
            }

            .btn-secondary {
                background: rgba(255, 255, 255, 0.58);
                color: #46546b;
            }

            .btn-secondary:hover:not(:disabled) {
                border-color: #aab6c8;
                background: rgba(255, 255, 255, 0.92);
            }

            .btn-track {
                border-color: #26364f;
                background: #26364f;
                color: #ffffff;
            }

            .btn-track:hover:not(:disabled) {
                background: #354a68;
            }

            .btn-track.is-tracked {
                border-color: rgba(119, 164, 143, 0.42);
                background: rgba(226, 241, 232, 0.94);
                color: #396d58;
            }

            .btn-track.is-tracked:hover:not(:disabled) {
                border-color: rgba(93, 140, 124, 0.62);
                background: rgba(214, 235, 222, 0.98);
            }

            .status {
                display: none;
                margin-top: 12px;
                padding: 11px 13px;
                border: 1px solid rgba(119, 164, 143, 0.24);
                border-radius: 10px;
                background: rgba(234, 245, 239, 0.82);
                color: #416d59;
                font-size: 12px;
                line-height: 1.45;
            }

            .status.is-error {
                border-color: rgba(190, 110, 110, 0.24);
                background: rgba(252, 238, 238, 0.88);
                color: #945252;
            }

            .status.is-visible {
                display: block;
            }

            .loader {
                display: none;
                margin-top: 14px;
                color: #718097;
                font-size: 13px;
            }

            .loader.is-visible {
                display: block;
            }

            .results {
                margin-top: 18px;
                padding: 16px 2px 0;
                border-top: 1px solid rgba(203, 213, 225, 0.68);
            }

            .platform-header {
                margin: 16px 0 7px;
                color: #778398;
                font-size: 10px;
                font-weight: 750;
                letter-spacing: 0.1em;
                text-transform: uppercase;
            }

            .compare-item {
                margin-bottom: 7px;
                padding: 12px;
                border: 1px solid rgba(255, 255, 255, 0.86);
                border-radius: 11px;
                background: rgba(255, 255, 255, 0.56);
            }

            .item-title {
                color: #35435a;
                font-size: 12px;
                font-weight: 600;
                line-height: 1.45;
                overflow-wrap: anywhere;
            }

            .item-details {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 10px;
                margin-top: 8px;
            }

            .item-price {
                color: #315d50;
                font-size: 14px;
                font-weight: 750;
            }

            .lowest-badge {
                margin-left: 6px;
                padding: 2px 6px;
                border-radius: 999px;
                background: rgba(244, 235, 213, 0.86);
                color: #8a6c38;
                font-size: 9px;
                font-weight: 750;
                text-transform: uppercase;
            }

            .item-link {
                flex: 0 0 auto;
                color: #536e94;
                font-size: 11px;
                font-weight: 700;
                text-decoration: none;
            }

            .item-link:hover {
                color: #263f64;
                text-decoration: underline;
            }

            .bubble {
                position: fixed;
                top: 18px;
                right: 18px;
                z-index: 2147483647;
                display: none;
                align-items: center;
                justify-content: center;
                width: 56px;
                height: 56px;
                border: 1px solid rgba(255, 255, 255, 0.72);
                border-radius: 999px;
                background: rgba(38, 54, 79, 0.9);
                color: #ffffff;
                box-shadow: 0 10px 30px rgba(25, 39, 62, 0.22);
                backdrop-filter: blur(16px);
                cursor: pointer;
                font-family: inherit;
                font-size: 13px;
                font-weight: 750;
                line-height: 1;
            }

            .bubble.is-visible {
                display: flex;
            }

            @media (max-width: 900px) {
                .panel {
                    width: 34vw;
                    min-width: 300px;
                }
            }

            @media (max-width: 560px) {
                .panel {
                    width: 100vw;
                    min-width: 0;
                    max-width: none;
                    border-radius: 0;
                }
            }

            @media (prefers-reduced-motion: reduce) {
                .panel,
                .btn {
                    transition: none;
                }
            }
        </style>

        <aside id="pricehawk-panel" class="panel" aria-label="PriceHawk panel">
            <header class="topbar">
                <div>
                    <div class="brand">PriceHawk</div>
                    <div id="ph-platform" class="platform">Shopping assistant</div>
                </div>
                <button id="ph-minimize" class="icon-btn" type="button" title="Minimize" aria-label="Minimize">-</button>
            </header>

            <main class="content">
                <section class="product">
                    <div id="ph-product-name" class="product-name">Loading...</div>
                    <div class="meta-row">
                        <span id="ph-platform-badge" class="badge">-</span>
                        <span id="ph-current-price" class="price">-</span>
                    </div>
                </section>

                <div id="ph-status" class="status"></div>
                <div id="ph-loader" class="loader">Comparing prices...</div>

                <div class="actions">
                    <button id="ph-compare" class="btn btn-primary" type="button">Instant Compare</button>
                    <button id="ph-track" class="btn btn-track" type="button">Track This Product</button>
                    <button id="ph-dashboard" class="btn btn-secondary" type="button">Open Dashboard</button>
                </div>

                <section id="ph-results" class="results" hidden>
                    <div id="ph-platform-results"></div>
                </section>
            </main>
        </aside>

        <button id="pricehawk-bubble" class="bubble" type="button" title="Open PriceHawk" aria-label="Open PriceHawk">PH</button>
    `;

    document.documentElement.appendChild(host);

    const state = {
        host,
        shadow,
        data: null
    };

    window.__priceHawkPanelState = state;

    shadow.getElementById("ph-minimize").addEventListener("click", minimizePriceHawkPanel);
    shadow.getElementById("pricehawk-bubble").addEventListener("click", () => {
        refreshPriceHawkProduct();
        openPriceHawkPanel();
    });
    shadow.getElementById("ph-track").addEventListener("click", trackPriceHawkProduct);
    shadow.getElementById("ph-compare").addEventListener("click", comparePriceHawkProduct);
    shadow.getElementById("ph-dashboard").addEventListener("click", () => {
        window.open("http://localhost:4200", "_blank", "noopener");
    });

    return state;
}

function openPriceHawkPanel() {
    const state = ensurePriceHawkPanel();
    state.shadow.getElementById("pricehawk-panel").classList.add("is-open");
    state.shadow.getElementById("pricehawk-bubble").classList.remove("is-visible");
}

function minimizePriceHawkPanel() {
    const state = ensurePriceHawkPanel();
    state.shadow.getElementById("pricehawk-panel").classList.remove("is-open");
    state.shadow.getElementById("pricehawk-bubble").classList.add("is-visible");
}

function refreshPriceHawkProduct() {
    const state = ensurePriceHawkPanel();
    const data = extractData();
    state.data = data;
    renderPriceHawkProduct(data);

    if (data.platform) {
        chrome.runtime.sendMessage({ action: "sendToBackend", data }, (response) => {
            if (response?.success && response.result && state.data?.platformId === data.platformId) {
                state.data.productId = response.result.id;
                state.data.isTracked = Boolean(response.result.isTracked);
                updatePriceHawkTrackButton();
            }
        });
    }
}

function renderPriceHawkProduct(data) {
    const state = ensurePriceHawkPanel();
    const shadow = state.shadow;
    const supported = Boolean(data.platform);

    shadow.getElementById("ph-product-name").textContent = supported
        ? data.name || "Unknown Product"
        : "Open a supported product page.";
    shadow.getElementById("ph-current-price").textContent = supported
        ? formatPriceHawkPrice(data.currentPrice)
        : "-";
    shadow.getElementById("ph-platform").textContent = supported
        ? `${data.platform} product`
        : "Unsupported page";
    shadow.getElementById("ph-platform-badge").textContent = supported
        ? data.platform
        : "Unsupported";

    shadow.getElementById("ph-track").disabled = !supported;
    shadow.getElementById("ph-compare").disabled = !supported;
    updatePriceHawkTrackButton();
    showPriceHawkStatus("", false);
}

function trackPriceHawkProduct() {
    const state = ensurePriceHawkPanel();
    const product = state.data || extractData();

    if (!product.platform) {
        showPriceHawkStatus("Open Amazon or Flipkart product page first.", true);
        return;
    }

    const isTracked = !Boolean(product.isTracked);
    const trackButton = state.shadow.getElementById("ph-track");
    trackButton.disabled = true;
    showPriceHawkStatus(isTracked ? "Enabling tracking..." : "Disabling tracking...", false);

    chrome.runtime.sendMessage(
        { action: "trackProduct", product: { ...product, isTracked } },
        (response) => {
            if (chrome.runtime.lastError) {
                trackButton.disabled = false;
                showPriceHawkStatus("Tracking failed: extension messaging issue.", true);
                return;
            }

            if (response?.success) {
                if (response.result?.id) {
                    state.data.productId = response.result.id;
                }
                state.data.isTracked = Boolean(response.result?.isTracked ?? isTracked);
                trackButton.disabled = false;
                updatePriceHawkTrackButton();
                showPriceHawkStatus(state.data.isTracked ? "Tracking enabled." : "Tracking disabled.", false);
                return;
            }

            trackButton.disabled = false;
            showPriceHawkStatus("Tracking failed.", true);
        }
    );
}

function updatePriceHawkTrackButton() {
    const state = ensurePriceHawkPanel();
    const button = state.shadow.getElementById("ph-track");
    const isTracked = Boolean(state.data?.isTracked);
    button.textContent = isTracked ? "Untrack This Product" : "Track This Product";
    button.classList.toggle("is-tracked", isTracked);
    button.setAttribute("aria-label", button.textContent);
}

function comparePriceHawkProduct() {
    const state = ensurePriceHawkPanel();
    const product = state.data || extractData();

    if (!product.platform) {
        showPriceHawkStatus("Open Amazon or Flipkart product page first.", true);
        return;
    }

    setPriceHawkLoading(true);
    showPriceHawkStatus("", false);

    chrome.runtime.sendMessage(
        {
            action: "compareProduct",
            data: {
                title: product.name,
                productId: product.productId
            }
        },
        (response) => {
            setPriceHawkLoading(false);

            if (chrome.runtime.lastError || !response?.success) {
                showPriceHawkStatus("Comparison failed.", true);
                return;
            }

            renderPriceHawkCompareResults(response.result?.platforms || {});
        }
    );
}

function renderPriceHawkCompareResults(platforms) {
    const state = ensurePriceHawkPanel();
    const results = state.shadow.getElementById("ph-results");
    const container = state.shadow.getElementById("ph-platform-results");
    container.textContent = "";

    const entries = Object.entries(platforms);
    if (!entries.length) {
        const none = document.createElement("div");
        none.className = "compare-item";
        none.textContent = "No exact matches found.";
        container.appendChild(none);
        results.hidden = false;
        return;
    }

    entries.forEach(([platform, data]) => {
        const header = document.createElement("div");
        header.className = "platform-header";
        header.textContent = platform;
        container.appendChild(header);

        const list = data?.best || [];
        if (!list.length) {
            const none = document.createElement("div");
            none.className = "compare-item";
            none.textContent = "No exact matches found.";
            container.appendChild(none);
            return;
        }

        list.forEach((item) => {
            const row = document.createElement("div");
            row.className = "compare-item";

            const name = document.createElement("div");
            name.className = "item-title";
            name.textContent = item.name || item.title || "Unknown Product";
            row.appendChild(name);

            const details = document.createElement("div");
            details.className = "item-details";

            const priceWrap = document.createElement("div");
            const price = document.createElement("span");
            price.className = "item-price";
            price.textContent = formatPriceHawkPrice(item.price);
            priceWrap.appendChild(price);

            if (item.is_lowest) {
                const badge = document.createElement("span");
                badge.className = "lowest-badge";
                badge.textContent = "Lowest";
                priceWrap.appendChild(badge);
            }

            const link = document.createElement("a");
            link.className = "item-link";
            link.href = item.productUrl || item.url || "#";
            link.target = "_blank";
            link.rel = "noopener noreferrer";
            link.textContent = "Store";

            details.appendChild(priceWrap);
            details.appendChild(link);
            row.appendChild(details);
            container.appendChild(row);
        });
    });

    results.hidden = false;
}

function setPriceHawkLoading(isLoading) {
    ensurePriceHawkPanel()
        .shadow
        .getElementById("ph-loader")
        .classList
        .toggle("is-visible", isLoading);
}

function showPriceHawkStatus(message, isError) {
    const status = ensurePriceHawkPanel().shadow.getElementById("ph-status");
    status.textContent = message;
    status.classList.toggle("is-visible", Boolean(message));
    status.classList.toggle("is-error", Boolean(isError));
}

function formatPriceHawkPrice(value) {
    const amount = Number(value);
    if (!Number.isFinite(amount) || amount <= 0) {
        return "Price NA";
    }

    return `Rs ${Math.round(amount).toLocaleString("en-IN")}`;
}

function extractData() {
    const url = window.location.href;
    const hostname = window.location.hostname.toLowerCase();

    let platform = null;
    let platformId = null;
    let name = null;
    let price = null;
    let imageUrl = null;
    let rating = null;
    let reviewCount = null;

    // ====================== AMAZON ======================
    if (hostname.includes("amazon")) {
        platform = "AMAZON";

        platformId = url.match(/\/dp\/([A-Z0-9]{10})/)?.[1]
            || url.match(/\/gp\/product\/([A-Z0-9]{10})/)?.[1]
            || null;

        name = document.querySelector("#productTitle")?.innerText?.trim();
        let priceText =
            document.querySelector("#corePrice_feature_div span.a-offscreen")?.innerText ||
            document.querySelector(".a-price-whole")?.innerText;

        if (priceText) {
            priceText = priceText.replace(/[^0-9.]/g, "").trim();
            const parsed = parseFloat(priceText);
            price = Number.isNaN(parsed) ? null : parsed;
        }


        imageUrl =
            document.querySelector("#landingImage")?.src ||
            document.querySelector("#imgTagWrapperId img")?.src;

        rating = document.querySelector(".a-icon-alt")?.innerText?.split(" ")[0];
        rating = rating ? parseFloat(rating) : null;

        reviewCount = document.querySelector("#acrCustomerReviewText")?.innerText?.replace(/[^0-9]/g, "");
        reviewCount = reviewCount ? parseInt(reviewCount) : null;
    }

    // ====================== FLIPKART ======================
    else if (hostname.includes("flipkart")) {
        platform = "FLIPKART";

        // ── Platform ID ───────────────────────────────────────────────────────
        platformId =
            url.match(/[?&]pid=([A-Z0-9]+)/i)?.[1] ||
            url.match(/\/p\/([^/?&#]+)/)?.[1] ||
            url.match(/\/pid\/([^/?&#]+)/)?.[1] ||
            url.match(/product-reviews\/([^/?&#]+)/)?.[1] ||
            null;

        const ldScript = document.querySelector("script[type='application/ld+json']#jsonLD");
        if (ldScript) {
            try {
                const jsonData = JSON.parse(ldScript.textContent);
                const products = Array.isArray(jsonData) ? jsonData : [jsonData];

                for (const product of products) {
                    if (product["@type"] !== "Product") continue;

                    // Extract name
                    name = product.name?.trim() || name;

                    // Extract image (array of URLs)
                    if (Array.isArray(product.image) && product.image.length > 0) {
                        imageUrl = product.image[0];
                    } else if (typeof product.image === "string") {
                        imageUrl = product.image;
                    }

                    if (product.offers && typeof product.offers === "object") {
                        const p = product.offers.price;
                        if (p != null) {
                            price = parseFloat(String(p).replace(/,/g, ""));
                        }
                    }

                    if (product.aggregateRating && typeof product.aggregateRating === "object") {
                        const rv = product.aggregateRating.ratingValue;
                        if (rv != null) {
                            rating = parseFloat(rv);
                        }
                        const rc = product.aggregateRating.reviewCount;
                        if (rc != null) {
                            reviewCount = parseInt(String(rc).replace(/,/g, ""));
                        }
                    }

                    break;
                }
            } catch (e) {
                console.error("JSON-LD parse error:", e);
            }
        }

    }

    return {
        platform,
        platformId,
        name: name || "Unknown Product",
        currentPrice: price != null ? Math.round(price) : 0,
        imageUrl,
        productUrl: url,
        rating,
        reviewCount
    };
}
