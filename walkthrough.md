# PriceHawk - Intelligent Shopping Assistant Walkthrough

## 🚀 System Overview
PriceHawk is a full-stack advanced shopping assistant that includes:
- **Chrome Extension**: Scrapes data from Amazon/Flipkart.
- **Backend (Spring Boot)**: Manages users, products, and notifications.
- **AI Service (Python/FastAPI)**: Predicts price drops and detects fake reviews.
- **Frontend (Angular)**: Visualizes price history and deals.
- **Database (MySQL)**: Stores persistent data.

## 🛠️ Setup Instructions

### 1. Database Setup
1. Ensure MySQL is running on port 3306.
2. Run the schema script:
   ```sql
   source d:/Projects/PriceHawk/database/schema.sql
   ```

### 2. Backend Setup (Spring Boot)
1. Navigate to `/backend`.
2. Update `src/main/resources/application.properties` with your MySQL password and Email credentials.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### 3. AI Service Setup (Python)
1. Navigate to `/ai-service`.
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   playwright install
   ```
3. Run the FastAPI server:
   ```bash
   python main.py
   ```

### 4. Frontend Setup (Angular)
1. Navigate to `/frontend`.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   ng serve
   ```
4. Open [http://localhost:4200](http://localhost:4200).

### 5. Chrome Extension Setup
1. Open Chrome and navigate to `chrome://extensions`.
2. Enable "Developer Mode" (top right).
3. Click "Load unpacked" and select `d:/Projects/PriceHawk/extension`.
4. Go to an Amazon product page to test.

## ✅ Verification Checklist

| Component | Feature | Verification |
|-----------|---------|--------------|
| **Extension** | Scrape Data | Click extension icon on Amazon page. Should see Name & Price. |
| **Backend** | API | Check `http://localhost:8080/api/products` returns list. |
| **AI** | Prediction | Extension popup should show "BUY" or "WAIT" advice |
| **Frontend** | Chart | "Compare" page should show a line graph of price history. |
| **Notification**| Email | Triggers when price drops (Simulate by updating DB). |

## 🧩 Key Architecture Features
- **Microservices-style**: Decoupled AI and Backend communication.
- **Scheduler**: `ai-service/scheduler.py` runs every 6 hours to update prices.
- **Confidence Scoring**: LSTM Predictor returns reliability metric.
- **Security**: JWT Authentication flow.
