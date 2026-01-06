
import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import uvicorn
from search_scraper import search_flipkart,search_amazon
from compare_engine import pick_best
import subprocess
import json
from scraper import (
    scrape_amazon,
    scrape_flipkart,
    scrape_croma,
    scrape_meesho
)

app = FastAPI(title="PriceHawk AI Service", version="1.0.0")


class ScrapeRequest(BaseModel):
    url: str
    platform: str


@app.get("/")
def root():
    return {"status": "PriceHawk AI Running"}

class CompareRequest(BaseModel):
    title: str


@app.post("/instant-compare")
def instant_compare(req: CompareRequest):

    query = req.title 

    a = search_amazon(query)
    f = search_flipkart(query)
    print("Amazon Results:", len(a))
    print(a)
    all_results = a+f

    result = pick_best(all_results)

    return result

@app.post("/scrape")
def scrape_endpoint(request: ScrapeRequest):

    result = subprocess.check_output(
        ["py", "scrape_runner.py", request.platform, request.url],
        shell=True
    )

    return json.loads(result)


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000)
