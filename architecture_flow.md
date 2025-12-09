# PriceHawk System Architecture

## 🔄 System Data Flow

```mermaid
graph TD
    User((User))
    
    subgraph "Client Side"
        FE[Angular Dashboard]
        Ext[Chrome Extension]
    end
    
    subgraph "Backend Layer"
        API[Spring Boot REST API]
        Auth[JWT Security]
        Email[Email Service]
    end
    
    subgraph "Data Layer"
        DB[(MySQL Database)]
    end
    
    subgraph "AI & Scraping Layer"
        AI[Python FastAPI Service]
        Scraper[Playwright Scraper]
        Scheduler[APScheduler]
        Predictor[LSTM Price Model]
        Review[Fake Review NLP]
    end
    
    %% Flows
    User -->|Views/Manages| FE
    User -->|Browses Shop| Ext
    
    FE -->|HTTP Requests| API
    Ext -->|Extracts Data| API
    
    API -->|Read/Write| DB
    API -->|Triggers| AI
    
    Scheduler -->|Triggers| Scraper
    Scraper -->|Scrapes HTML| Amazon_Flipkart[Amazon / Flipkart]
    Scraper -->|Returns Data| AI
    
    AI -->|Updates| DB
    AI -->|Provides Insights| API
    
    Predictor -->|Trends| AI
    Review -->|Scores| AI
    
    Email -->|Alerts| User
    
    %% Styling
    style User fill:#f9f,stroke:#333,stroke-width:2px
    style DB fill:#569,stroke:#333,stroke-width:2px,color:white
    style Amazon_Flipkart fill:#ff9900,stroke:#333,stroke-width:2px
```

## 🧩 Module Interaction Detail

### 1. Data Ingestion Flow
1. **Trigger**: User views product (Extension) OR Scheduler runs (AI Service).
2. **Action**: `Scraper` fetches HTML from Amazon/Flipkart.
3. **Processing**: Data extracted -> Sent to `AI Service`.
4. **Storage**: `AI Service` updates `Products` and `PriceHistory` tables in MySQL.

### 2. User Interaction Flow
1. **Login**: User logs in via Angular (`/api/auth/signin`).
2. **Dashboard**: Fetches products (`/api/products`).
3. **Comparison**: Frontend requests `/api/products/{id}/best-deal`.
4. **Logic**: Backend compares prices in DB and returns the best option.

### 3. Notification Flow
1. **Trigger**: Scheduler updates a price.
2. **Check**: Backend checks `Watchlist` table for `current_price <= target_price`.
3. **Action**: `EmailService` sends HTML email via JavaMail.
