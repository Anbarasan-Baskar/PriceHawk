import re

class FakeReviewDetector:
    def __init__(self):
        # We would load a trained BERT/RoBERTa model here
        pass

    def analyze_reviews(self, reviews: list):
        """
        Analyzes a list of review texts for authenticity.
        Returns a fake_score (0-100), where 100 is highly likely fake.
        """
        if not reviews:
            return {"fake_score": 0, "summary": "No reviews to analyze"}

        total_reviews = len(reviews)
        suspicious_count = 0
        reasons = []

        # Heuristic 1: Duplicate Content
        unique_reviews = set(reviews)
        if len(unique_reviews) < total_reviews:
            suspicious_count += (total_reviews - len(unique_reviews))
            reasons.append("Duplicate review content detected")

        # Heuristic 2: Extreme Lengths (Very short generic reviews)
        short_reviews = [r for r in reviews if len(r.split()) < 3]
        if len(short_reviews) > total_reviews * 0.3:
            suspicious_count += len(short_reviews) * 0.5
            reasons.append("High volume of very short reviews")

        # Heuristic 3: Overuse of marketing keywords (Mock)
        marketing_keywords = ["best product", "must buy", "amazing", "100%", "guaranteed"]
        keyword_hits = 0
        for r in reviews:
            if any(k in r.lower() for k in marketing_keywords):
                keyword_hits += 1
        
        if keyword_hits > total_reviews * 0.5:
             suspicious_count += keyword_hits * 0.2
             reasons.append("Unnatural marketing language detected")

        # Calculate Score
        score = min(100, (suspicious_count / total_reviews) * 100)
        
        return {
            "fake_score": round(score, 2),
            "summary": "; ".join(reasons) if reasons else "Reviews appear organic"
        }

review_detector = FakeReviewDetector()
