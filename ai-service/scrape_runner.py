import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

import sys
import json
from scraper import (
    scrape_amazon,
    scrape_flipkart,
    scrape_croma,
    scrape_meesho
)

platform = sys.argv[1].upper()
url = sys.argv[2]

if platform == "AMAZON":
    result = scrape_amazon(url)

elif platform == "FLIPKART":
    result = scrape_flipkart(url)

elif platform == "CROMA":
    result = scrape_croma(url)

elif platform == "MEESHO":
    result = scrape_meesho(url)

else:
    result = {"error": "Unsupported platform"}

print(json.dumps(result))
