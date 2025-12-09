🔥 User Journey — New User Using PriceHawk Extension

(Full clickable flow with actions + UI + backend interactions)

Step 1 — User Installs Extension

📌 Chrome Web Store → installs PriceHawk – Intelligent Price Tracker

UI seen in browser toolbar:

🦅 PriceHawk icon added near URL bar

Step 2 — User opens Amazon / Flipkart product page

Example page:

https://www.amazon.in/dp/B0DHDDF5J2


User clicks 🦅 PriceHawk icon

Popup UI visible:

----------------------------------------------------
   🦅 PriceHawk
----------------------------------------------------
 Product Name: Loading...
 Current Price: Loading...

 [Track Price]      [View Dashboard]
----------------------------------------------------
 Prediction: -
 Confidence: -
----------------------------------------------------

Step 3 — Popup requests data from content script

popup.js --> content.js

chrome.tabs.sendMessage({action: "extract"})

Backend workflow begins

content.js -> background.js -> backend /api/scrape/update

Step 4 — Extracted Product Data filled in popup

Popup becomes:

----------------------------------------------------
 🦅 PriceHawk
----------------------------------------------------
 Product Name: boAt Bassheads 900 Pro...
 Current Price: ₹999
 Rating: ⭐ 4.3 / 5
 Reviews: 2,714

 [Track Price]      [View Dashboard]
----------------------------------------------------
 Prediction: Fetching...
----------------------------------------------------

Track Price button now enabled
Step 5 — User clicks “Track Price”

Button → background.js POST:

POST /api/scrape/update
{
 platform: "AMAZON",
 platformId: "B0DHDDF5J2",
 name: "...",
 currentPrice: 999,
 imageUrl: "...",
 rating: 4.3,
 reviewCount: 2714,
 productUrl: "https://www.amazon.in/dp/B0DHDDF5J2",
 isTracked: true
}

Success toast
🎉 Product added to watchlist!


Button changes:

[Tracking Enabled]      [View Dashboard]

Step 6 — User clicks "View Dashboard"

Opens Angular frontend:

http://localhost:4200

Dashboard UI
------------------------------------------------------
|  🖼 Image      boAt Bassheads 900 Pro               |
|  ⭐ 4.3 | 2714 reviews | Amazon | ₹999              |
|  [View History]   [Compare Price]    [Set Alert]   |
------------------------------------------------------

Step 7 — User clicks “View History”

Calls:

GET /api/history/{productId}


Shows graph:
📈 Price History Line Graph

And buttons:

[Compare Price]    [AI Prediction]

Step 8 — User selects AI Prediction

Calls:

POST /api/ai/predict


UI result:

 Trend: DOWN 📉
 Recommended: WAIT
 Confidence: 92%

Step 9 — User sets Alert

Modal:

Enter target price: [ 899 ]
[Save Alert]


POST:

POST /api/watchlist/set-alert


Scheduler will notify when applicable:

📩 Email / popup alert when price <= 899

🧠 Final Real Flow Diagram
Page → Extension Icon Click → Extract Data → Save Product + PriceHistory
→ Track Price Button → Dashboard → History Graph → AI Prediction → Alerts

🚀 What’s the MOST IMPORTANT NEXT feature to build

Dashboard → View History Graph (GET /api/history/{productId})

Because:

without price history = no AI, no comparison, no alerts

users must see value instantly