import asyncio
from playwright.async_api import async_playwright
from datetime import datetime

async def scrape_product(url: str, platform: str):
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        page = await browser.new_page(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )
        
        try:
            await page.goto(url, timeout=30000)
            
            data = {}
            if platform.upper() == "AMAZON":
                data = await extract_amazon(page)
            elif platform.upper() == "FLIPKART":
                data = await extract_flipkart(page)
            
            data['url'] = url
            data['platform'] = platform
            data['scraped_at'] = datetime.now().isoformat()
            
            return data
            
        except Exception as e:
            return {"error": str(e)}
        finally:
            await browser.close()

async def extract_amazon(page):
    title = await page.locator("#productTitle").text_content()
    
    # Try multiple price selectors
    price_whole = await page.locator(".a-price-whole").first.text_content()
    # clean price
    price = price_whole.replace(",", "").replace(".", "").strip() if price_whole else "0"
    
    image = await page.locator("#landingImage").get_attribute("src")
    rating = await page.locator("#acrPopover").get_attribute("title")
    
    return {
        "name": title.strip() if title else "Unknown",
        "current_price": float(price),
        "image_url": image,
        "rating": rating.split(" ")[0] if rating else "0.0"
    }

async def extract_flipkart(page):
    title = await page.locator(".B_NuCI").text_content() # Class might change, strict selectors needed
    price = await page.locator("._30jeq3._16Jk6d").text_content()
    
    # Clean price (remove ₹ and commas)
    price_val = price.replace("₹", "").replace(",", "").strip() if price else "0"
    
    image = await page.locator("._396cs4._2amPTt._3qGmMb").first.get_attribute("src")
    
    return {
        "name": title.strip() if title else "Unknown",
        "current_price": float(price_val),
        "image_url": image,
        "rating": "4.5" # Placeholder as Flipkart classes are very dynamic
    }
