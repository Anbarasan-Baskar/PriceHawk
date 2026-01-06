def pick_best(results,
                             min_confidence=0.35,
                             max_results_per_platform=3):
    """
    results: list of dicts produced by search_* functions
    returns: dict grouped by platform with sorted best matches
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

        # 1) filter garbage
        valid = [
            i for i in items
            if i.get("price") is not None
            and i.get("confidence", 0) >= min_confidence
        ]

        if not valid:
            final_output[platform] = []
            continue

        # 2) sort: higher confidence first, then lower price
        valid.sort(
            key=lambda x: (-x["confidence"],
                           x["price"] if x["price"] is not None else 10**9)
        )

        # 3) take top N per platform
        final_output[platform] = valid[:max_results_per_platform]

    return {
        "platforms": final_output
    }
