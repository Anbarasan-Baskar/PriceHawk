from fastapi import FastAPI, BackgroundTasks
from pydantic import BaseModel
import uvicorn
from contextlib import asynccontextmanager

app = FastAPI(title="PriceHawk AI Service", version="1.0.0")

class ScrapeRequest(BaseModel):
    url: str
    platform: str # "AMAZON" or "FLIPKART"

@app.get("/")
def read_root():
    return {"status": "AI Service Running", "models": ["LSTM_Price", "NLP_Fake_Review"]}

from scraper import scrape_product
from predictor import predictor
from fake_review import review_detector
from typing import List

class PriceHistoryItem(BaseModel):
    price: float
    date: str

class PredictRequest(BaseModel):
    history: List[PriceHistoryItem]

class ReviewRequest(BaseModel):
    reviews: List[str]

@app.post("/scrape")
async def scrape_endpoint(request: ScrapeRequest):
    result = await scrape_product(request.url, request.platform)
    return result

@app.post("/predict/price")
def predict_price_endpoint(request: PredictRequest):
    # Convert Pydantic models to dict
    history = [{"price": item.price, "date": item.date} for item in request.history]
    return predictor.predict_next_price(history)

@app.post("/detect/fake-reviews")
def detect_fake_endpoint(request: ReviewRequest):
    return review_detector.analyze_reviews(request.reviews)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load models here
    print("Loading AI Models...")
    from scheduler import start_scheduler
    start_scheduler()
    yield
    print("Shutting down...")

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
