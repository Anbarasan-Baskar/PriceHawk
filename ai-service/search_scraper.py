import re
import sys
import traceback
from difflib import SequenceMatcher
from playwright.sync_api import sync_playwright
from normalize import normalize_title, normalize_for_search

def similarity(a, b):
    if not a or not b:
        return 0.0
    return SequenceMatcher(None, a.lower(), b.lower()).ratio()


def clean_price(text):
    if not text:
        return None
    text = text.replace("₹", "").replace(",", "").strip()
    m = re.search(r"\d+\.?\d*", text)
    return float(m.group(0)) if m else None


# ================= AMAZON ================= #

def search_amazon(query):
    norm_query = normalize_title(query)
    search_q = normalize_for_search(query)
    url = f"https://www.amazon.in/s?k={search_q.replace(' ', '+')}"
    print("Amazon Search URL:", url)

    with sync_playwright() as p:
        browser = p.chromium.launch(
            headless=False,
            slow_mo=200,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"]
        )

        # Create a stealthy context: realistic UA, viewport, locale and init scripts
        context = browser.new_context(
            user_agent=("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"),
            viewport={"width": 1280, "height": 800},
            locale="en-US"
        )

        # Basic anti-detection script (reduces common Playwright fingerprints)
        stealth_js = """
        Object.defineProperty(navigator, 'webdriver', {get: () => false});
        window.navigator.chrome = { runtime: {} };
        Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});
        Object.defineProperty(navigator, 'plugins', {get: () => [1,2,3,4,5]});
        """
        context.add_init_script(stealth_js)

        page = context.new_page()

        page.goto(url, timeout=60000)
        page.wait_for_timeout(2500)

        results = []

        page.wait_for_selector("div[data-component-type='s-search-result']")

        items = page.locator("div[data-component-type='s-search-result']").all()

        print("AMAZON ITEMS FOUND:", len(items))

        for idx, it in enumerate(items[:10], start=1):
            print("Processing item", idx)

            try:
                titles = it.locator("h2").all_inner_texts()
                title = " ".join(" ".join(titles).split())

                price = (
                    it.locator("span.a-price-whole").first.text_content()
                    or it.locator("span.a-offscreen").first.text_content()
                )

                link = it.locator("a.a-link-normal.s-no-outline").first.get_attribute("href")

                image = it.locator("img.s-image").first.get_attribute("src")

                print("TITLE:", normalize_title(title), "| PRICE RAW:", price)

                results.append({
                    "platform": "AMAZON",
                    "title": title.strip(),
                    "price": clean_price(price),
                    "image": image,
                    "url": "https://www.amazon.in" + link,
                    "confidence": round(similarity(normalize_title(title), norm_query), 3)
                })

            except Exception as e:
                print("Item failed:", e)
                continue



        try:
            context.close()
        except Exception:
            pass

        try:
            browser.close()
        except Exception:
            pass
        return results


# ================= FLIPKART ================= #

def search_flipkart(query):
    norm_query = normalize_title(query)
    search_q = normalize_for_search(query)
    url = f"https://www.flipkart.com/search?q={search_q.replace(' ', '+')}"
    print("Flipkart Search URL:", url)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False, slow_mo=200)
        page = browser.new_page()
        page.goto(url, timeout=60000)

        page.wait_for_timeout(2000)

        results = []

        cards = page.locator("div[data-id]").all()
        print("Cards Found:", len(cards))

        for idx, c in enumerate(cards[:20], start=1):
            print(f"Processing Card #{idx}")

            try:
                title = ( c.locator("a[title]").first.text_content()
                          or c.locator(".atJtCj").first.text_content()
                        ).strip()

                price = c.locator(".hZ3P6w").first.text_content()

                link = c.locator("a").first.get_attribute("href")

                image = c.locator("img").first.get_attribute("src")

                print("TITLE:", title)
                print("PRICE:", price)

                results.append({
                    "platform": "FLIPKART",
                    "title": title,
                    "price": clean_price(price),
                    "image": image,
                    "url": "https://www.flipkart.com" + link,
                    "confidence": round(similarity(normalize_title(title), norm_query), 3)
                })

            except Exception as e:
                print("Card failed:", e)
                continue

        browser.close()
        return results


# ================= CLI TEST ================= #

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('Usage: py search_scraper.py "search query"')
        sys.exit(1)

    q = " ".join(sys.argv[1:])
    print(">>> Searching Amazon for:", q)
    print(">>> Searching Flipkart for:", q)
    try:
        res = search_amazon(q)
        res = search_flipkart(q)
        print("Results:", len(res))

        if res:
            import json
            print(json.dumps(res[:3], indent=2))

    except Exception:
        traceback.print_exc()
        sys.exit(1)
