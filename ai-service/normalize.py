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
