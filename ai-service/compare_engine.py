def pick_best(results,
                             min_confidence=0.35,
                             max_results_per_platform=5):
    """
    results: list of dicts produced by search_* functions
    returns: dict grouped by platform with filtered, normalized candidates.

    For each platform we return a simple list (max 5) of candidates sorted by
    confidence desc then price asc. This service's responsibility is ONLY to
    scrape, normalize and return candidates — all comparison/selection logic
    (best/lowest selection) must be performed in the Spring backend.
    """

    if not results:
        return {"error": "no results", "platforms": {}}

    grouped = {}

    # group by platform
    for r in results:
        platform = r.get("platform", "UNKNOWN")

        if platform not in grouped:
            grouped[platform] = []

        grouped[platform].append(r)

    final_output = {}

    for platform, items in grouped.items():

        # 1) filter garbage & normalize numeric fields
        valid = []
        for i in items:
            price = i.get("price")
            confidence = i.get("confidence", 0)

            # normalize price to float if possible
            try:
                if price is not None and not isinstance(price, (int, float)):
                    price = float(str(price).replace(',', '').strip())
                i["price"] = float(price) if price is not None else None
            except Exception:
                i["price"] = None

            try:
                i["confidence"] = float(confidence)
            except Exception:
                i["confidence"] = 0.0

            if i["price"] is not None and i["confidence"] >= min_confidence:
                valid.append(i)

        if not valid:
            final_output[platform] = []
            continue

        # sort: confidence desc, then price asc
        valid.sort(
            key=lambda x: (-x["confidence"], x["price"] if x["price"] is not None else 10**9)
        )

        # take up to N candidates
        final_output[platform] = valid[:max_results_per_platform]

    return {
        "platforms": final_output
    }
