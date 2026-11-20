chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {

    if (request.action === "sendToBackend") {
        fetch("http://127.0.0.1:8080/api/scrape/save", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request.data)
        })
            .then(r => r.json())
            .then(result => sendResponse({ success: true, result }))
            .catch(err => sendResponse({ success: false, error: err.toString() }));
        return true;
    }

    if (request.action === "trackProduct") {
        console.log("Track Product Response:", request.data);
        const params = new URLSearchParams({
            pid: request.product.platformId,
            platform: request.product.platform,
            isTracked: String(request.product.isTracked ?? true)
        });

        fetch(`http://127.0.0.1:8080/api/scrape/track?${params.toString()}`, {
            method: "PATCH"
        })
            .then(async (res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const result = await res.json();
                console.log("Track Product Result:", result);
                sendResponse({ success: true, result });
            })
            .catch((err) => {
                console.log(request.data);
                console.error("TrackProduct Fetch Error:", err);
                sendResponse({ success: false, error: err.toString() });
            });

        return true;
    }

    if (request.action === "compareProduct") {

        const params = new URLSearchParams({ title: request.data.title });
        if (request.data.productId != null) {
            params.set("productId", String(request.data.productId));
        }

        fetch(`http://localhost:8080/api/compare/instant?${params.toString()}`, {
            method: "GET"
        })
            .then(async (res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                if (res.status === 204) {
                    return { platforms: {}, error: "No comparison results returned." };
                }
                return res.json();
            })
            .then((result) => sendResponse({ success: true, result }))
            .catch((err) => sendResponse({ success: false, error: err.toString() }));

        return true; // keep message channel open
    }



});

chrome.action.onClicked.addListener((tab) => {
    if (!tab?.id || !/^https?:\/\//.test(tab.url || "")) {
        return;
    }

    const message = { action: "togglePriceHawkPanel" };

    chrome.tabs.sendMessage(tab.id, message, () => {
        if (!chrome.runtime.lastError) {
            return;
        }

        chrome.scripting.executeScript(
            { target: { tabId: tab.id }, files: ["content.js"] },
            () => {
                if (chrome.runtime.lastError) {
                    console.error("PriceHawk panel injection failed:", chrome.runtime.lastError.message);
                    return;
                }

                chrome.tabs.sendMessage(tab.id, message);
            }
        );
    });
});
