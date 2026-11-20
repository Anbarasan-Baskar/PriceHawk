from collections import defaultdict
import math
import re


def _to_confidence(value) -> float:
    if value is None or isinstance(value, bool):
        return 0.0
    try:
        val = float(value)
        return val if math.isfinite(val) else 0.0
    except (ValueError, TypeError):
        return 0.0


def pick_best(
    results: list[dict],
    min_confidence: float = 0.35,
    max_results_per_platform: int = 5,
) -> dict:
    if not results:
        return {"error": "no results", "platforms": {}}

    grouped = defaultdict(list)
    for r in results:
        platform = str(r.get("platform") or "UNKNOWN").upper()
        grouped[platform].append(r)

    final_output = {}

    for platform, items in grouped.items():
        valid = []
        seen_keys = set()

        for raw in items:

            confidence = _to_confidence(raw.get("confidence", 0.0))

            if confidence < min_confidence:
                continue

            dedupe_key = (
                raw.get("platformId")
                or raw.get("pid")
                or raw.get("productUrl")
                or raw.get("url")
            )
            if dedupe_key:
                if dedupe_key in seen_keys:
                    continue
                seen_keys.add(dedupe_key)

            item = dict(raw)
            item["platform"] = platform
            item["confidence"] = confidence
            valid.append(item)

        valid.sort(key=lambda x: (-x["confidence"], x["price"]))
        final_output[platform] = valid[:max_results_per_platform]

    return {"platforms": final_output}
