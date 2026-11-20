import re

STOPWORDS = {
    "with",
    "for",
    "and",
    "the",
    "new",
    "latest",
    "from",
    "by",
    "men",
    "women",
    "boys",
    "girls",
    "free",
    "size",
    "pack",
    "set",
}

COLOR_WORDS = {
    "black",
    "white",
    "blue",
    "red",
    "green",
    "yellow",
    "pink",
    "purple",
    "grey",
    "gray",
    "silver",
    "gold",
    "golden",
    "titanium",
    "midnight",
    "starlight",
    "graphite",
    "sierra",
    "alpine",
    "phantom",
    "cream",
    "lavender",
    "mint",
    "coral",
    "orange",
    "brown",
    "bronze",
    "onyx",
    "marble",
    "cobalt",
    "violet",
    "indigo",
    "cyan",
    "teal",
    "beige",
}


ACCESSORY_PATTERN = re.compile(
    r"\b(?:"
    r"cases?|covers?|back\s*covers?|flip\s*covers?|pouches?|"
    r"screen\s*(?:guards?|protectors?)|tempered\s*glass(?:es)?|"
    r"straps?|watch\s*bands?|chargers?|charging\s*cables?|data\s*cables?|"
    r"adapters?|docks?|stands?|holders?|skins?|decals?|lens\s*protectors?"
    r")\b",
    flags=re.IGNORECASE,
)


SPEC_PATTERN = re.compile(
    r"\b(\d+(?:\.\d+)?)\s*(gb|tb|mb|mah|w|hz|inch|inches|cm|mm|ton|tons|l|ltr)\b",
    flags=re.IGNORECASE,
)

SINGLE_LETTER_SERIES = {"z", "s", "g", "a", "m", "x", "r", "c", "y", "v", "f"}


def is_accessory(text: str) -> bool:
    if not text:
        return False
    return bool(ACCESSORY_PATTERN.search(text))


def _normalize_units(text: str) -> str:
    def _repl(match: re.Match) -> str:
        num = match.group(1)
        unit = match.group(2).lower()
        if unit in {"inches"}:
            unit = "inch"
        elif unit in {"tons"}:
            unit = "ton"
        elif unit in {"ltr"}:
            unit = "l"
        return f"{num}{unit}"

    return SPEC_PATTERN.sub(_repl, text)


def normalize_title(title: str) -> str:
    if not title:
        return ""

    t = _normalize_units(title.lower())

    # Extract specs inside brackets before stripping bracketed fluff/colors
    bracket_specs = SPEC_PATTERN.findall(t)
    preserved_specs = [f"{num}{unit.lower()}" for num, unit in bracket_specs]

    # Remove parenthetical/bracketed descriptions
    t = re.sub(r"\([^)]*\)|\[[^\]]*\]", " ", t)
    t = re.sub(r"[^a-z0-9.]", " ", t)
    t = re.sub(r"\s+", " ", t).strip()

    words = []
    for w in t.split():
        if w in STOPWORDS or w in COLOR_WORDS:
            continue
        if len(w) > 1 or w in SINGLE_LETTER_SERIES or w.isdigit():
            words.append(w)

    # Re-append extracted storage/RAM specs if not already present
    for spec in preserved_specs:
        if spec not in words:
            words.append(spec)

    return " ".join(words)


def normalize_for_search(title: str, max_words: int = 6) -> str:
    if not title:
        return ""

    t = _normalize_units(title.lower())

    # Keep primary storage/capacity token (e.g., 128gb, 1.5ton) if present in brackets
    bracket_specs = [
        f"{num}{unit.lower()}"
        for num, unit in SPEC_PATTERN.findall(t)
        if unit.lower() in {"gb", "tb", "ton", "tons", "inch", "inches"}
    ]

    # 1. Remove bracketed content (colors, secondary specs)
    t = re.sub(r"\([^)]*\)|\[[^\]]*\]", " ", t)

    # 2. Normalize shorthand and separators
    t = re.sub(r"\bw/\b|\bw\.\b|/", " ", t)
    t = re.sub(r"turn[- ]?by[- ]?turn", "turn navigation", t)
    t = re.sub(r"[-_]+", " ", t)
    t = re.sub(r"[^a-z0-9.]", " ", t)
    t = re.sub(r"\s+", " ", t).strip()

    search_stopwords = (
        STOPWORDS
        | COLOR_WORDS
        | {
            "ai",
            "smartphone",
            "mobile",
            "phone",
            "features",
            "feature",
            "face",
            "studio",
            "emergency",
            "sos",
            "qr",
            "tray",
            "active",
            "refurbished",
            "renewed",
            "unboxed",
            "edition",
            "series",
        }
    )

    raw_words = [
        w
        for w in t.split()
        if w not in search_stopwords
        and (len(w) > 1 or w in SINGLE_LETTER_SERIES or w.isdigit())
    ]

    # 3. Merge 'smart watch' -> 'smartwatch' and remove consecutive duplicates
    compact = []
    i = 0
    while i < len(raw_words):
        w = raw_words[i]
        if w == "smart" and i + 1 < len(raw_words) and raw_words[i + 1] == "watch":
            token = "smartwatch"
            i += 2
        else:
            token = w
            i += 1
        if not compact or compact[-1] != token:
            compact.append(token)

    model_modifiers = {
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
        "prime",
        "neo",
        "5g",
        "4g",
    }

    seeds = compact[:3]
    idx = 3
    while idx < len(compact) and (
        compact[idx] in model_modifiers or any(c.isdigit() for c in compact[idx])
    ):
        seeds.append(compact[idx])
        idx += 1

    # Add primary storage spec
    for spec in bracket_specs[:1]:
        if spec not in seeds:
            seeds.append(spec)

    feature_priority = [
        "navigation",
        "turn",
        "bt",
        "bluetooth",
        "calling",
        "smartwatch",
        "qled",
        "oled",
        "4k",
        "wireless",
        "earbuds",
        "anc",
    ]

    rest = [w for w in compact[idx:] if w not in seeds]
    features = [w for w in rest if w in feature_priority]

    final = (seeds + features)[:max_words]
    for w in rest:
        if len(final) >= max_words:
            break
        if w not in final:
            final.append(w)

    return " ".join(final[:max_words])
