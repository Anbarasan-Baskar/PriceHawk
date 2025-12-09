document.getElementById('dashboardBtn').addEventListener('click', () => {
    window.open('http://localhost:4200', '_blank');
});
let extractedData = null;
document.addEventListener('DOMContentLoaded', async () => {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });

    if (!tab.url.includes("amazon") && !tab.url.includes("flipkart")) {
        document.getElementById('productName').innerText = "Not a supported product page.";
        document.getElementById('trackBtn').style.display = 'none';
        return;
    }

    chrome.tabs.sendMessage(tab.id, { action: "extract" }, (response) => {
        if (!response) {
            console.log(response);
            console.error("Content script did not respond.");
            document.getElementById('productName').innerText = "Failed to extract data.";
            document.getElementById('currentPrice').innerText = "-";
            return;
        }

        // Safe update
        extractedData = response;
        document.getElementById('productName').innerText =
            (response.name ? response.name.substring(0, 50) : "Unknown Product") + "...";

        document.getElementById('currentPrice').innerText =
            response.currentPrice ? '' + response.currentPrice : "Price NA";
    });
});

document.getElementById("trackBtn").addEventListener("click", () => {
    if (!extractedData) {
        alert("No data available to track.");
        return;
    }

    chrome.runtime.sendMessage(
        { action: "trackProduct", product: extractedData },
        (response) => {
            if (chrome.runtime.lastError) {
                console.error("Runtime message error:", chrome.runtime.lastError.message);
                alert("Tracking failed - extension messaging issue.");
                return;
            }

            if (response?.success) {
                alert("Tracking Enabled!");
            } else {
                alert("Tracking Failed.");
            }
        }
    );
});

document.getElementById("compareBtn").addEventListener("click", () => {
    fetch(`http://localhost:8080/api/compare?platform=${data.platform}&platformId=${data.platformId}`)
        .then(res => res.json())
        .then(compare => {
            document.getElementById("compareResult").style.display = "block";
            document.getElementById("bestPlatform").innerText = compare.best;
            document.getElementById("difference").innerText = compare.difference;
        })
        .catch(err => console.error("Compare error:", err));
});

