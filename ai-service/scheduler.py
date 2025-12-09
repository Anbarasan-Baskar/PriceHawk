from apscheduler.schedulers.asyncio import AsyncIOScheduler
from scraper import scrape_product
from predictor import predictor
import asyncio
import mysql.connector
import os

# Database Connection
def get_db_connection():
    return mysql.connector.connect(
        host="localhost",
        user="root",
        password="1234",
        database="pricehawk"
    )

scheduler = AsyncIOScheduler()

async def update_tracked_products():
    print("Starting scheduled scraping task...")
    try:
        conn = get_db_connection()
        cursor = conn.cursor(dictionary=True)
        
        # Get all tracked products
        cursor.execute("SELECT id, platform_id, platform, product_url FROM products WHERE is_tracked = 1")
        products = cursor.fetchall()
        
        for p in products:
            print(f"Scraping {p['id']}: {p['product_url']}")
            data = await scrape_product(p['product_url'], p['platform'])
            
            if 'error' not in data:
                current_price = data['current_price']
                
                # Update Product Table
                update_query = "UPDATE products SET current_price = %s, last_updated = NOW() WHERE id = %s"
                cursor.execute(update_query, (current_price, p['id']))
                
                # Add to History
                history_query = "INSERT INTO price_history (product_id, price) VALUES (%s, %s)"
                cursor.execute(history_query, (p['id'], current_price))
                
                print(f"Updated product {p['id']}: ₹{current_price}")
                
        conn.commit()
        cursor.close()
        conn.close()
        print("Scheduled scraping task completed.")
        
    except Exception as e:
        print(f"Error in scheduler: {e}")

# Start Scheduler
def start_scheduler():
    scheduler.add_job(update_tracked_products, 'interval', hours=6)
    scheduler.start()
