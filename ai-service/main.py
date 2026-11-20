import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional
import uvicorn
from search_scraper import search_flipkart,search_amazon
from compare_engine import pick_best
import subprocess
import json
import traceback
from pathlib import Path

app = FastAPI(title="PriceHawk AI Service", version="1.0.0")


class ScrapeRequest(BaseModel):
    url: str
    platform: str


@app.get("/")
def root():
    return {"status": "PriceHawk AI Running"}

class CompareRequest(BaseModel):
    title: str
    productId: Optional[int] = None


@app.post("/instant-compare")
def instant_compare(req: CompareRequest):
    query = req.title.strip()
    if not query:
        return {"error": "title is required", "platforms": {}}

    all_results = []
    errors = {}

    try:
        all_results.extend(search_amazon(query))
    except Exception as e:
        traceback.print_exc()
        errors["AMAZON"] = str(e)

    try:
        all_results.extend(search_flipkart(query))
    except Exception as e:
        traceback.print_exc()
        errors["FLIPKART"] = str(e)

    result = pick_best(all_results)
    if errors:
        result["errors"] = errors
    print("Best Result:", result)
    return result

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000)
 