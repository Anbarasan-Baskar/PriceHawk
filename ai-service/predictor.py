import numpy as np
from datetime import datetime, timedelta
import random

# Placeholder for a real LSTM model
# In production, we would load: model = load_model('price_lstm.h5')

class PricePredictor:
    def __init__(self):
        self.model = None # Would load here

    def predict_next_price(self, price_history: list):
        """
        Predicts the next price based on a list of historical prices.
        Args:
            price_history: List of dictionaries [{'price': 100, 'date': '2023-01-01'}]
        Returns:
            dict: {predicted_price, confidence_score, trend}
        """
        if not price_history or len(price_history) < 2:
            return {
                "predicted_price": 0,
                "confidence_score": 0,
                "trend": "INSUFFICIENT_DATA"
            }

        prices = [p['price'] for p in price_history]
        current_price = prices[-1]

        # MOCK LOGIC: Simple Moving Average + Random Fluctuation for Demo
        # In real LSTM: Scale data -> sequences -> model.predict -> inverse_scale
        
        avg_price = sum(prices) / len(prices)
        
        # Simulate a prediction slightly lower or higher
        # biased towards a drop if current is high vs average
        if current_price > avg_price:
            predicted_change = random.uniform(-0.05, 0.01) # Likely to drop
            trend = "DOWN"
        else:
            predicted_change = random.uniform(-0.01, 0.05) # Likely to rise
            trend = "UP"
            
        predicted_price = current_price * (1 + predicted_change)
        
        # Confidence score based on variance (lower variance = higher confidence)
        variance = np.var(prices)
        confidence = max(0, min(100, 100 - (variance / avg_price)))

        return {
            "predicted_price": round(predicted_price, 2),
            "confidence_score": round(confidence, 2),
            "trend": trend,
            "prediction_date": (datetime.now() + timedelta(days=7)).strftime("%Y-%m-%d")
        }

predictor = PricePredictor()
