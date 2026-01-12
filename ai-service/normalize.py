import re

# words useless for matching
STOPWORDS = {
    "with", "for", "and", "the", "new", "latest",
    "edition", "series", "model", "smartwatch",
    "watch", "men", "women", "boys", "girls",
    "premium", "pro", "neo", "edition", "combo",
    "pack", "set"
}

# accessory keywords we DON'T want treated as same product
ACCESSORY_KEYWORDS = {
    "case", "cover", "screen", "protector", "guard",
    "strap", "band", "charger", "cable", "stand", "dock"
}


def is_accessory(text: str) -> bool:
    text = text.lower()
    return any(k in text for k in ACCESSORY_KEYWORDS)


def normalize_title(title: str) -> str:
    if not title:
        return ""

    t = title.lower()

    # remove bracket content (often useless variants)
    t = re.sub(r"\(.*?\)|\[.*?\]", " ", t)

    # remove size specs like 1.39", 3.5cm, 44mm
    t = re.sub(r"\d+(\.\d+)?\s*(cm|mm|inch|in|\")", " ", t)

    # remove extra symbols
    t = re.sub(r"[^a-z0-9 ]", " ", t)

    # collapse multiple spaces
    t = re.sub(r"\s+", " ", t).strip()

    # split → remove stopwords
    words = [
        w for w in t.split()
        if w not in STOPWORDS and len(w) > 1
    ]

    return " ".join(words)


def normalize_for_search(title: str, max_words: int = 8) -> str:
    """
    Create a compact search query from a raw product title suitable for building site search URLs.
    - lowercases, removes bracketed content, measurements and marketing noise
    - maps common terms (e.g., 'bluetooth' -> 'bt') and collapses noisy words
    - keeps a concise list of core identity words (brand + model + main feature)
    - limits output to `max_words` (defaults to 8)
    """
    if not title:
        return ""

    t = title.lower()

    # remove bracket content and measurements
    t = re.sub(r"\(.*?\)|\[.*?\]", " ", t)
    t = re.sub(r"\d+(\.\d+)?\s*(cm|mm|inch|in|\")", " ", t)

    # normalize common shorthand and separators
    t = re.sub(r"\bw/\b|\bw\.\b|/", " ", t)
    t = re.sub(r"\bbluetooth\b", "bt", t)

    # collapse 'turn-by-turn' phrasing early so it tokenizes correctly
    t = re.sub(r"turn[- ]?by[- ]?turn", "turn navigation", t)

    t = re.sub(r"[-_]+", " ", t)

    # remove other non-alphanumeric chars
    t = re.sub(r"[^a-z0-9 ]", " ", t)
    t = re.sub(r"\s+", " ", t).strip()

    # words to drop for search queries (marketing noise, colors, generic words)
    SEARCH_STOPWORDS = {
        "with", "for", "and", "the", "new", "latest", "edition", "series", "model",
        "men", "women", "boys", "girls", "premium", "pro", "neo", "combo", "pack", "set",
        "features", "feature", "face", "studio", "emergency", "sos", "qr", "tray",
        "active", "black", "white", "red", "blue", "green", "strap", "free", "size",
        "inch", "inches", "display", "hd", "battery", "charger", "cable",
        "pack", "stand", "dock", "case", "cover", "protector", "guard"
    }

    # drop very short tokens and stopwords
    raw_words = [w for w in t.split() if len(w) > 1 and w not in SEARCH_STOPWORDS]

    # merge 'smart watch' -> 'smartwatch'
    merged = []
    i = 0
    while i < len(raw_words):
        w = raw_words[i]
        if w == "smart" and i + 1 < len(raw_words) and raw_words[i + 1] == "watch":
            merged.append("smartwatch")
            i += 2
            continue
        merged.append(w)
        i += 1

    # remove consecutive duplicates (e.g., 'turn turn navigation' -> 'turn navigation')
    compact = []
    prev = None
    for w in merged:
        if w == prev:
            continue
        compact.append(w)
        prev = w

    # prefer early brand/model tokens, then important feature tokens, then fill up to max_words
    # keep the first 3 tokens (brand + model pieces) as seeds so model names like 'lunar discovery' are preserved
    seeds = compact[:3]

    FEATURE_PRIORITY = [
        "navigation", "turn", "bt", "calling", "smartwatch", "phone", "camera", "qled", "4k", "wireless", "earbuds"
    ]

    rest = [w for w in compact[2:] if w not in seeds]

    features = []
    for w in rest:
        if w in FEATURE_PRIORITY and w not in features:
            features.append(w)

    final = seeds + features

    # fill with remaining tokens in order until max_words
    for w in rest:
        if len(final) >= max_words:
            break
        if w not in final:
            final.append(w)

    # final cleanup: trim to max_words
    final = final[:max_words]

    return " ".join(final)


if __name__ == "__main__":
    # small demo for quick manual checks
    examples = [
        'boAt Lunar Discovery w/ 1.39" (3.5 cm) HD Display, Turn-by-Turn Navigation, DIY Watch Face Studio, Bluetooth Calling, Emergency SOS, QR Tray, Smart Watch for Men & Women (Active Black)',
        'Apple iPhone 14 Pro Max (256 GB) - Space Black',
        'Samsung 65-inch QLED 4K Smart TV (QN65Q80A) with HDR'
    ]
    for e in examples:
        print("RAW:", e)
        print("SEARCH QUERY:", normalize_for_search(e))
        print("NORMALIZED TITLE:", normalize_title(e))
        print()
