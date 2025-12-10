chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "extract") {
        const data = extractData();
        sendResponse(data);
        chrome.runtime.sendMessage({ action: "sendToBackend", data });
    }
    return true;
});

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
            // Remove ₹ and commas, KEEP decimal point
            priceText = priceText.replace(/[₹,]/g, "").trim();  // "589.00" -> "589.00"
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

        platformId = url.match(/\/p\/([^/?]+)/)?.[1] || null;

        name =
            document.querySelector(".B_NuCI")?.innerText?.trim() ||
            document.querySelector(".LMizgS")?.innerText?.trim();

        price =
            document.querySelector("._30jeq3._16Jk6d")?.innerText?.replace(/[^0-9]/g, "") ||
            document.querySelector("div.hZ3P6w.bnqy13")?.innerText?.replace(/[^0-9]/g, "");

        imageUrl =
            document.querySelector("img._396cs4._2amPTt._3qGmMb")?.src ||
            document.querySelector("img.UCc1lI")?.src;

        // Rating out of 5 (not rating count)
        rating = parseFloat(document.querySelector(".MKiFS6")?.innerText?.trim()) || null;

        // Ratings & Reviews using regex
        const ratingWrapper = document.querySelector(".PvbNMB");
        if (ratingWrapper) {
            const text = ratingWrapper.innerText;
            const ratingCountMatch = text.match(/([\d,]+)\s*Ratings/i);
            const reviewCountMatch = text.match(/([\d,]+)\s*Reviews/i);

            if (ratingCountMatch) reviewCount = parseInt(ratingCountMatch[1].replace(/,/g, ""));
            else if (reviewCountMatch) reviewCount = parseInt(reviewCountMatch[1].replace(/,/g, ""));
        }
    }

    // ====================== MYNTRA ======================
    else if (hostname.includes("myntra")) {
        platform = "MYNTRA";

        platformId = url.match(/(\d+)/)?.[1] || null;

        name = document.querySelector(".pdp-title")?.innerText?.trim();

        price = document.querySelector(".pdp-price strong")?.innerText?.replace(/[^0-9]/g, "");

        imageUrl = document.querySelector("img.img-responsive")?.src;

        rating = parseFloat(document.querySelector("span.index-overallRating")?.innerText) || null;

        reviewCount =
            parseInt(document.querySelector("span.index-ratingsCount")?.innerText?.replace(/[^0-9]/g, "")) || null;
    }

    // ====================== MEESHO ======================
    else if (hostname.includes("meesho")) {
        platform = "MEESHO";

        platformId = url.match(/\/p\/([^/?]+)/)?.[1] || null;

        name = document.querySelector(".pdp-title")?.innerText?.trim();

        price = document.querySelector("span.pdp-price")?.innerText?.replace(/[^0-9]/g, "");

        imageUrl = document.querySelector("img[data-testid='product-image']")?.src;

        rating = parseFloat(document.querySelector("span[data-testid='overall-rating']")?.innerText) || null;

        reviewCount =
            parseInt(document.querySelector("span[data-testid='ratings-count']")?.innerText?.replace(/[^0-9]/g, "")) ||
            null;
    }

    // ====================== SNAPDEAL ======================
    else if (hostname.includes("snapdeal")) {
        platform = "SNAPDEAL";

        platformId = url.match(/\/product\/[^\/]+\/(\d+)/)?.[1] || null;

        name = document.querySelector(".pdp-details-title")?.innerText?.trim();

        price = document.querySelector("span.payBlkBig")?.innerText?.replace(/[^0-9]/g, "");

        imageUrl = document.querySelector("#image-block img.cloudzoom")?.src;

        rating = parseFloat(document.querySelector(".avrg-rating")?.innerText) || null;

        reviewCount = parseInt(document.querySelector("span.hd-review-count")?.innerText?.replace(/[^0-9]/g, "")) || null;
    }

    return {
        platform,
        platformId,
        name: name || "Unknown Product",
        currentPrice: price ? parseInt(price) : 0,
        imageUrl,
        productUrl: url,
        rating,
        reviewCount
    };
}
