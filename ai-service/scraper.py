
import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

from playwright.sync_api import sync_playwright


def scrape_amazon(url: str):
    # No need to reset the event loop policy here; the module-level policy is sufficient.

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )

        page.goto(url, timeout=60000)

        title = page.locator("#productTitle").inner_text()

        price_text = page.locator(".a-price .a-offscreen").first.inner_text()
        price = float(price_text.replace("₹", "").replace(",", "").strip())

        image = page.locator("#landingImage").get_attribute("src")

        browser.close()

        return {
            "platform": "AMAZON",
            "title": title.strip(),
            "image": image,
            "price": price
        }


from playwright.sync_api import sync_playwright
import asyncio
asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

def scrape_flipkart(url: str):
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page(
            user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        )

        page.goto(url, timeout=60000, wait_until="networkidle")

        # platformId
        import re
        match = re.search(r"/p/([^/?]+)", url)
        platform_id = match.group(1) if match else None

        # name
        name = (
            page.locator(".B_NuCI").first.inner_text() 
            if page.locator(".B_NuCI").count() > 0 
            else page.locator(".LMizgS").first.inner_text() if page.locator(".LMizgS").count() > 0 
            else None
        )

        # price
        price_el = None
        if page.locator("._30jeq3._16Jk6d").count() > 0:
            price_el = page.locator("._30jeq3._16Jk6d").first.inner_text()
        elif page.locator("div.hZ3P6w.bnqy13").count() > 0:
            price_el = page.locator("div.hZ3P6w.bnqy13").first.inner_text()

        price = None
        if price_el:
            clean = "".join([c for c in price_el if c.isdigit()])
            price = int(clean) if clean else None

        # image
        image = None
        if page.locator("img._396cs4._2amPTt._3qGmMb").count() > 0:
            image = page.locator("img._396cs4._2amPTt._3qGmMb").first.get_attribute("src")
        elif page.locator("img.UCc1lI").count() > 0:
            image = page.locator("img.UCc1lI").first.get_attribute("src")

        # rating
        rating = None
        if page.locator(".MKiFS6").count() > 0:
            try:
                rating = float(page.locator(".MKiFS6").first.inner_text())
            except:
                rating = None

        # review count
        review_count = None
        if page.locator(".PvbNMB").count() > 0:
            text = page.locator(".PvbNMB").first.inner_text()
            import re
            m = re.search(r"([\d,]+)\s*Reviews", text)
            if m:
                review_count = int(m.group(1).replace(",", ""))

        browser.close()

        return {
            "platform": "FLIPKART",
            "platformId": platform_id,
            "title": name or "",
            "image": image or "",
            "price": price or 0,
            "rating": rating,
            "reviewCount": review_count,
            "productUrl": url
        }



def scrape_croma(url: str):
    # No need to reset the event loop policy here; the module-level policy is sufficient.

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        page.goto(url, timeout=60000)

        title = page.locator("h1").first.inner_text()

        price_text = page.locator(".pdp-price, .amount").first.inner_text()
        price = float(price_text.replace("₹", "").replace(",", "").strip())

        image = page.locator("meta[property='og:image']").get_attribute("content")

        browser.close()

        return {
            "platform": "CROMA",
            "title": title.strip(),
            "image": image,
            "price": price
        }


def scrape_meesho(url: str):
    # No need to reset the event loop policy here; the module-level policy is sufficient.

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        page.goto(url, timeout=60000)

        title = page.locator("meta[property='og:title']").get_attribute("content")
        image = page.locator("meta[property='og:image']").get_attribute("content")
        price_text = page.locator("meta[property='product:price:amount']").get_attribute("content")

        browser.close()

        return {
            "platform": "MEESHO",
            "title": title or "",
            "image": image or "",
            "price": float(price_text) if price_text else 0.0
        }
