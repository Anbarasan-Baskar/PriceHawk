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
                document.getElementById("trackBtn").innerText = "Tracking...";
                alert("Tracking Enabled!");
            } else {
                alert("Tracking Failed.");
            }
        }
    );
});

document.getElementById("compareBtn").addEventListener("click", () => {

    if (!extractedData) {
        alert("No data available to compare.");
        return;
    }

    document.getElementById('loader').style.display = 'block';

    chrome.runtime.sendMessage(
        {
            action: "compareProduct",
            data: {
                title: extractedData.title || extractedData.name,
                platform: extractedData.platform,
                platformId: extractedData.platformId
            }
        },
        (response) => {
            document.getElementById('loader').style.display = 'none';

            if (!response || !response.success) {
                console.error("Compare error:", response?.error);
                alert('Comparison failed.');
                return;
            }

            const compare = response.result;
            renderCompareResults(compare.platforms || {});
        }
    );

});

function renderCompareResults(platforms) {
    const container = document.getElementById('platformResults');
    container.innerHTML = '';

    Object.entries(platforms).forEach(([platform, data]) => {
        const platDiv = document.createElement('div');
        platDiv.style.borderTop = '1px solid #eee';
        platDiv.style.paddingTop = '8px';

        const title = document.createElement('div');
        title.style.fontWeight = 'bold';
        title.innerText = platform;
        platDiv.appendChild(title);

        const list = (data && data.best) ? data.best : [];

        if (!list.length) {
            const none = document.createElement('div');
            none.innerText = 'No matches found.';
            platDiv.appendChild(none);
        } else {
            list.forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.style.marginTop = '6px';

                const name = document.createElement('div');
                name.innerText = (item.name || item.title || '').substring(0, 80);
                itemDiv.appendChild(name);

                const price = document.createElement('div');
                price.innerHTML = `<span style="font-weight:bold;color:#27ae60">₹${item.price}</span>` + (item.is_lowest ? ' <small style="color:#e67e22">— Lowest</small>' : '');
                itemDiv.appendChild(price);

                const link = document.createElement('a');
                link.href = item.productUrl || item.url || '#';
                link.target = '_blank';
                link.innerText = 'View';
                itemDiv.appendChild(link);

                platDiv.appendChild(itemDiv);
            });
        }

        container.appendChild(platDiv);
    });

    document.getElementById('compareResult').style.display = 'block';
}


