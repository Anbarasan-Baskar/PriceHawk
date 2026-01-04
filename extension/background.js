chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {

    if (request.action === "sendToBackend") {
        fetch("http://127.0.0.1:8080/api/scrape/update", {
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

        const payload = {
            platform: request.product.platform,
            platformId: request.product.platformId,
            name: request.product.name,
            currentPrice: request.product.currentPrice,
            imageUrl: request.product.imageUrl,
            productUrl: request.product.productUrl,
            rating: request.product.rating,
            reviewCount: request.product.reviewCount,
            isTracked: true   
        };

        fetch("http://127.0.0.1:8080/api/scrape/update", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
            .then(async (res) => {
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const result = await res.json();
                sendResponse({ success: true, result });
            })
            .catch((err) => {
                console.log(payload);
                console.error("TrackProduct Fetch Error:", err);
                sendResponse({ success: false, error: err.toString() });
            });

        return true; // Keep message channel open for async
    }

    if (request.action === "compareProduct") {

    fetch(`http://127.0.0.1:8080/api/compare?platform=${request.data.platform}&platformId=${request.data.platformId}`)
        .then(res => res.json())
        .then(result => {
            sendResponse({ success: true, result });
        })
        .catch(err => {
            console.error("Compare API error:", err);
            sendResponse({ success: false, error: err.toString() });
        });

    return true; // keep channel alive
}



});
