# PriceHawk Implementation Plan

## Goal Description
Build "PriceHawk", a comprehensive intelligent shopping assistant system that compares prices, tracks history, predicts drops, and detects fake reviews across Amazon and Flipkart.

## Architecture Overview
The system follows a microservices-like architecture:
1.  **Frontend**: Angular 18 Dashboard.
2.  **Extension**: Chrome Manifest V3.
3.  **Backend**: Spring Boot 4.0.0 (Java 21).
4.  **AI/Scraper Service**: Python FastAPI.
5.  **Database**: MySQL 8.

## Proposed Changes

### Project Structure
Root: `d:/Projects/PriceHawk`
- `/backend`: Spring Boot Application.
- `/frontend`: Angular Application.
- `/ai-service`: Python FastAPI (AI models + Scraper).
- `/extension`: Chrome Extension source.
- `/database`: SQL scripts.

### Database Design
- **Users**: ID, email, password_hash, role.
- **Products**: ID, platform_id (asin/fsn), name, image_url, current_price, rating, platform (Amazon/Flipkart).
- **PriceHistory**: ID, product_id, price, timestamp.
- **Watchlist**: ID, user_id, product_id, target_price.
- **Predictions**: ID, product_id, predicted_price, confidence, timestamp.

### Backend (Spring Boot)
- **Tech**: Java 21, Spring Boot 4.0+, Spring Data JPA, Spring Security (JWT), **JavaMailSender**.
- **Key Modules**:
    - **Email Notification Engine**:
        - Monitor watchlist items.
        - Trigger emails when `current_price` <= `target_price`.
        - Use HTML templates for "Price Drop Alert".
    - **Comparison Algorithm**:
        - Normalize product names from Amazon/Flipkart.
        - Match based on identical model numbers or fuzzy string matching (if ASIN/FSN mapping isn't available).
- **Endpoints**:
    - `/auth/*`: Login/Register.
    - `/products/*`: CRUD, Search, Comparison.
    - `/watchlist/*`: User watchlist management.
    - `/history/*`: Price history data.
- **Integration**: Scheduled tasks (`@Scheduled`) to trigger Python scraper updates.

### AI Service (Python)
- **Tech**: Python 3.14, FastAPI, Playwright, TensorFlow/PyTorch (LSTM), Scikit-Learn/NLTK.
- **AI Operations**:
    - **Price Prediction**:
        - **Model**: LSTM (Long Short-Term Memory) trained on historical price sequences.
        - **Confidence Metrics**: Return a probability score (0-100%) alongside the prediction.
    - **Fake Review Detection**:
        - NLP scoring model to analyze review sentiment and patterns (e.g., repeated phrases, burstiness).
- **Scraper Scheduling**:
    - Use `APScheduler` in Python or trigger via Spring Boot cron jobs.
    - Rate limiting to avoid IP bans (use rotating user agents).
- **Endpoints**:
    - `/scrape`: Trigger scraping for a product URL.
    - `/predict/price`: Get price forecast + confidence.
    - `/detect/fake-reviews`: Analyze reviews.

### Frontend (Angular)
- **Tech**: Angular 18, Chart.js, Vanilla CSS.
- **Key Views**:
    - **Product Comparison**: Side-by-side view of Amazon vs Flipkart with "Best Buy" highlighted.
    - **Price History Graph**: Interactive Chart.js line graph showing historical trends and projected future drop.
    - **AI Recommendation**: Detailed page explaining the "Buy" or "Wait" signal with confidence intervals.

### Chrome Extension
- **Tech**: HTML/JS, Manifest V3.
- **Internal Flow**:
    1.  **Content Script**: Detects product page load. Extracts `Title`, `Price`, `Image`, `PlatformID`.
    2.  **Background Service Worker**: Receives data, checks with Backend API if product exists.
    3.  **Popup UI**:
        - If tracked: Show min price, price history mini-chart, and "Wait/Buy" advice.
        - If not tracked: Button to "Track this Product".

## Verification Plan
### Automated Tests
- Backend: JUnit tests for Services (PriceLogic, WatchlistService).
- AI: PyTest for API endpoints and Model sanity checks (input shape/output range).
- Frontend: Basic component rendering tests.

### Manual Verification
1.  **Scraping & Comparison**: Verify extracting data from a live Amazon product page and correctly matching/comparing it.
2.  **Notifications**: Manually lower a product price in DB and verify Email Alert is received.
3.  **AI Insights**: Verify Prediction API returns a JSON with `price`, `trend`, and `confidence_score`.
4.  **UI Visualization**: Verify Price History graph renders multiple data points correctly.
5.  **Extension**: Verify popup shows data when on a product page and correctly sends data to backend.
