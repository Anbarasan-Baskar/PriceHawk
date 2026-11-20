import re
import sys
import traceback
import json
from difflib import SequenceMatcher
from html import unescape
from urllib.parse import parse_qs, urljoin, urlparse
from playwright.sync_api import sync_playwright
from normalize import normalize_title, normalize_for_search, is_accessory

try:
    from playwright_stealth import Stealth
except ImportError:
    Stealth = None

FLIPKART_BASE_URL = "https://flipkart.com"


def apply_stealth(page):
    if Stealth is None:
        return
    try:
        Stealth().apply_stealth_sync(page)
    except Exception as exc:
        print(f"Stealth setup skipped: {exc}")


CRITICAL_MODIFIERS = {
    "ultra",
    "pro",
    "max",
    "plus",
    "fe",
    "lite",
    "mini",
    "air",
    "note",
    "edge",
    "fold",
    "flip",
    "anc",
    "5g",
    "4g",
}
CONDITION_WORDS = {"refurbished", "renewed", "unboxed", "preowned"}
STORAGE_PATTERN = re.compile(r"\b\d+(?:gb|tb)\b")


def similarity(norm_query: str, norm_candidate: str) -> float:
    if not norm_query or not norm_candidate:
        return 0.0

    tokens_q = set(norm_query.split())
    tokens_c = set(norm_candidate.split())

    if (tokens_c & CONDITION_WORDS) != (tokens_q & CONDITION_WORDS):
        return 0.0

    storage_q = set(STORAGE_PATTERN.findall(norm_query))
    storage_c = set(STORAGE_PATTERN.findall(norm_candidate))
    if storage_q and storage_c and storage_q != storage_c:
        return 0.15

    mods_q = tokens_q & CRITICAL_MODIFIERS
    mods_c = tokens_c & CRITICAL_MODIFIERS
    if mods_q != mods_c:
        return 0.25

    intersection = len(tokens_q & tokens_c)
    if intersection == 0:
        return 0.0

    containment = intersection / min(len(tokens_q), len(tokens_c))
    dice = (2.0 * intersection) / (len(tokens_q) + len(tokens_c))

    return round(0.7 * containment + 0.3 * dice, 3)


def clean_price(text):
    if text is None or text == "":
        return None
    if isinstance(text, (int, float)):
        return float(text)
    text = str(text).replace("₹", "").replace(",", "").strip()
    m = re.search(r"\d+\.?\d*", text)
    return float(m.group(0)) if m else None


import json
import re
from urllib.parse import quote_plus
from playwright.sync_api import sync_playwright
from normalize import is_accessory, normalize_for_search, normalize_title


def search_flipkart(query: str, headless: bool = False) -> list[dict]:
    query_is_acc = is_accessory(query)
    norm_query = normalize_title(query)
    search_url = (
        f"https://www.flipkart.com/search?q={quote_plus(normalize_for_search(query))}"
    )
    print("Flipkart Search URL:", search_url)
    with sync_playwright() as p:
        browser = p.chromium.launch(
            headless=False,
            slow_mo=150,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
        )
        context = browser.new_context(
            user_agent=(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            ),
            viewport={"width": 1280, "height": 800},
            locale="en-IN",
        )
        page = context.new_page()
        apply_stealth(page)

        try:
            response = page.goto(
                search_url, timeout=60000, wait_until="domcontentloaded"
            )
            page.wait_for_timeout(1500)

            try:
                raw_html = response.text() if response else ""
            except Exception:
                raw_html = ""
            if "window.__INITIAL_STATE__" not in raw_html:
                raw_html = page.content()
        finally:
            context.close()
            browser.close()

    match = re.search(r"window\.__INITIAL_STATE__\s*=\s*", raw_html)
    if not match:
        return []

    try:
        state, _ = json.JSONDecoder().raw_decode(raw_html[match.end() :])
    except Exception:
        return []

    results, seen, stack = [], set(), [state]

    while stack:
        node = stack.pop()
        if isinstance(node, list):
            stack.extend(reversed(node))
            continue
        if not isinstance(node, dict):
            continue

        for item in node.get("products") or []:
            info = item.get("productInfo") or {} if isinstance(item, dict) else {}
            val = info.get("value")
            if not isinstance(val, dict):
                continue

            pid = val.get("id")
            if not pid or pid in seen:
                continue
            seen.add(pid)

            titles = val.get("titles") or {}
            title = titles.get("title") or titles.get("newTitle") or ""
            if not title or (not query_is_acc and is_accessory(title)):
                continue

            pricing = val.get("pricing") or {}
            prices = pricing.get("prices") or []
            active = next(
                (p for p in prices if not p.get("strikeOff")),
                prices[0] if prices else {},
            )
            mrp = next((p for p in prices if p.get("strikeOff")), {})
            price = (
                active.get("value")
                if active.get("value") is not None
                else (pricing.get("finalPrice") or {}).get("value")
            )

            rating = val.get("rating") or {}
            images = (val.get("media") or {}).get("images") or []
            raw_img = (
                images[0].get("url", "")
                if images and isinstance(images[0], dict)
                else val.get("imageUrl", "")
            )
            rel_url = val.get("baseUrl") or (info.get("action") or {}).get("url", "")
            full_url = (
                rel_url
                if rel_url.startswith("http")
                else f"https://www.flipkart.com{rel_url}"
            )

            results.append(
                {
                    "platform": "FLIPKART",
                    "pid": pid,
                    "platformId": pid,
                    "title": title,
                    "price": price,
                    "mrp": mrp.get("value"),
                    "rating": rating.get("average"),
                    "ratingCount": rating.get("count"),
                    "reviewCount": rating.get("reviewCount"),
                    "image": raw_img.replace("{@width}", "312")
                    .replace("{@height}", "312")
                    .replace("{@quality}", "80"),
                    "url": full_url,
                    "productUrl": full_url,
                    "confidence": round(
                        similarity(normalize_title(title), norm_query), 3
                    ),
                }
            )

        stack.extend(reversed(list(node.values())))

    results.sort(
        key=lambda x: (
            -x["confidence"],
            x["price"] if x["price"] is not None else float("inf"),
        )
    )
    return results


#  AMAZON


def search_amazon(query):
    query_is_acc = is_accessory(query)
    norm_query = normalize_title(query)
    search_q = normalize_for_search(query)
    url = f"https://amazon.in/s?k={search_q.replace(' ', '+')}"
    print("Amazon Search URL:", url)
    with sync_playwright() as p:
        browser = p.chromium.launch(
            headless=False,
            slow_mo=200,
            args=["--no-sandbox", "--disable-blink-features=AutomationControlled"],
        )
        context = browser.new_context(
            user_agent=(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            ),
            viewport={"width": 1280, "height": 800},
            locale="en-US",
        )
        page = context.new_page()
        apply_stealth(page)
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
                if not query_is_acc and is_accessory(title):
                    print(f"Skipping accessory: {title}")
                    continue
                price = (
                    it.locator("span.a-price-whole").first.text_content()
                    or it.locator("span.a-offscreen").first.text_content()
                )
                link = it.locator("a.a-link-normal.s-no-outline").first.get_attribute(
                    "href"
                )
                image = it.locator("img.s-image").first.get_attribute("src")
                print("TITLE:", normalize_title(title), "| PRICE RAW:", price)
                results.append(
                    {
                        "platform": "AMAZON",
                        "title": title.strip(),
                        "price": clean_price(price),
                        "image": image,
                        "url": "https://amazon.in" + link,
                        "confidence": round(
                            similarity(normalize_title(title), norm_query), 3
                        ),
                    }
                )
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


# ================= CLI TEST =================

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('Usage: py search_scraper.py "search query"')
        sys.exit(1)
    q = " ".join(sys.argv[1:])
    print(">>> Searching Amazon for:", q)
    print(">>> Searching Flipkart for:", q)
    try:
        res = search_flipkart(q)
        print("Results:", len(res))
        if res:
            print(json.dumps(res[:3], indent=2))
    except Exception:
        traceback.print_exc()
        sys.exit(1)
