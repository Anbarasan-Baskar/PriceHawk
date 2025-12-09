import requests
import json

url = "http://localhost:8080/api/products"
headers = {"Content-Type": "application/json"}
data = {
    "platformId": "TEST001",
    "platform": "AMAZON",
    "name": "PriceHawk Demo Product - iPhone 15",
    "imageUrl": "https://m.media-amazon.com/images/I/71d7rfSl0wL._AC_SX679_.jpg",
    "productUrl": "https://www.amazon.in/dp/TEST001",
    "currentPrice": 75000,
    "rating": 4.5
}

try:
    response = requests.post(url, headers=headers, json=data)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
except Exception as e:
    print(f"Error: {e}")
