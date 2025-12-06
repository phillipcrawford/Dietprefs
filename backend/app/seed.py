"""
Database seeding script for Dietprefs.
Creates realistic restaurants with diverse cuisines and menu items.
"""
import random
import math
from app.database import SessionLocal, engine, Base
from app.models.vendor import Vendor
from app.models.item import Item


# Realistic restaurant data by cuisine type
RESTAURANTS = [
    {
        "name": "Green Leaf Cafe",
        "cuisine": "Vegan",
        "tags": "vegan,breakfast,lunch,organic,healthy,plant-based",
        "items": [
            {"name": "Tofu Scramble Bowl", "vegan": True, "vegetarian": True, "gluten_free": True, "high_protein": True, "no_eggs": True, "no_milk": True, "price": 12.50},
            {"name": "Acai Berry Smoothie Bowl", "vegan": True, "vegetarian": True, "organic": True, "raw": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 10.00},
            {"name": "Quinoa Power Salad", "vegan": True, "vegetarian": True, "organic": True, "locally_sourced": True, "no_eggs": True, "no_milk": True, "price": 13.50},
            {"name": "Chickpea Buddha Bowl", "vegan": True, "vegetarian": True, "gluten_free": True, "high_protein": True, "no_eggs": True, "no_milk": True, "price": 14.00},
            {"name": "Vegan Chocolate Cake", "vegan": True, "vegetarian": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 7.50},
            {"name": "Green Goddess Wrap", "vegan": True, "vegetarian": True, "organic": True, "no_eggs": True, "no_milk": True, "price": 11.00},
            {"name": "Matcha Latte", "vegan": True, "vegetarian": True, "organic": True, "no_eggs": True, "no_milk": True, "price": 5.50},
        ]
    },
    {
        "name": "Tokyo Sushi Bar",
        "cuisine": "Japanese",
        "tags": "sushi,japanese,seafood,dinner,lunch,asian",
        "items": [
            {"name": "California Roll", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "price": 8.50},
            {"name": "Salmon Nigiri", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "price": 12.00},
            {"name": "Spicy Tuna Roll", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "price": 10.50},
            {"name": "Miso Soup", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 4.00},
            {"name": "Shrimp Tempura", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "price": 14.00},
            {"name": "Edamame", "vegan": True, "vegetarian": True, "gluten_free": True, "organic": True, "no_eggs": True, "no_milk": True, "price": 5.50},
            {"name": "Dragon Roll", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "price": 15.00},
        ]
    },
    {
        "name": "Bella Italia Pizzeria",
        "cuisine": "Italian",
        "tags": "italian,pizza,pasta,dinner,lunch,family-friendly",
        "items": [
            {"name": "Margherita Pizza", "vegetarian": True, "no_eggs": True, "entree": True, "price": 14.00},
            {"name": "Pepperoni Pizza", "beef": True, "pork": True, "entree": True, "price": 16.00},
            {"name": "Fettuccine Alfredo", "vegetarian": True, "entree": True, "price": 15.50},
            {"name": "Spaghetti Carbonara", "pork": True, "no_red_meat": True, "entree": True, "price": 16.50},
            {"name": "Caprese Salad", "vegetarian": True, "gluten_free": True, "organic": True, "no_eggs": True, "price": 9.00},
            {"name": "Tiramisu", "vegetarian": True, "sweet": True, "price": 8.50},
            {"name": "Chicken Parmigiana", "chicken": True, "no_pork_products": True, "entree": True, "high_protein": True, "price": 18.00},
        ]
    },
    {
        "name": "El Mariachi Mexican Grill",
        "cuisine": "Mexican",
        "tags": "mexican,tacos,burritos,dinner,lunch,spicy",
        "items": [
            {"name": "Carne Asada Tacos", "beef": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 12.00},
            {"name": "Chicken Burrito Bowl", "chicken": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 13.50},
            {"name": "Veggie Fajitas", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 11.00},
            {"name": "Fish Tacos", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "entree": True, "price": 14.00},
            {"name": "Guacamole & Chips", "vegan": True, "vegetarian": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 7.50},
            {"name": "Cheese Quesadilla", "vegetarian": True, "no_eggs": True, "price": 9.50},
            {"name": "Churros", "vegetarian": True, "sweet": True, "price": 6.00},
        ]
    },
    {
        "name": "The Breakfast Nook",
        "cuisine": "American Breakfast",
        "tags": "breakfast,brunch,american,coffee,pancakes",
        "items": [
            {"name": "Buttermilk Pancakes", "vegetarian": True, "sweet": True, "price": 9.50},
            {"name": "Bacon & Eggs", "pork": True, "high_protein": True, "keto": True, "low_carb": True, "entree": True, "price": 11.00},
            {"name": "Avocado Toast", "vegetarian": True, "vegan": True, "organic": True, "no_eggs": True, "no_milk": True, "price": 10.00},
            {"name": "Greek Yogurt Parfait", "vegetarian": True, "gluten_free": True, "organic": True, "no_eggs": True, "sweet": True, "price": 8.50},
            {"name": "Denver Omelet", "vegetarian": True, "gluten_free": True, "high_protein": True, "keto": True, "entree": True, "price": 12.50},
            {"name": "French Toast", "vegetarian": True, "sweet": True, "price": 10.50},
            {"name": "Breakfast Burrito", "pork": True, "beef": True, "entree": True, "high_protein": True, "price": 13.00},
        ]
    },
    {
        "name": "Golden Dragon Chinese",
        "cuisine": "Chinese",
        "tags": "chinese,asian,dinner,takeout,wok,noodles",
        "items": [
            {"name": "Kung Pao Chicken", "chicken": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 14.50},
            {"name": "Vegetable Lo Mein", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 11.00},
            {"name": "Sweet & Sour Pork", "pork": True, "sweet": True, "entree": True, "price": 13.50},
            {"name": "Beef with Broccoli", "beef": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 15.00},
            {"name": "Spring Rolls", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "price": 6.50},
            {"name": "General Tso's Chicken", "chicken": True, "no_pork_products": True, "sweet": True, "entree": True, "price": 14.00},
            {"name": "Fried Rice", "vegetarian": True, "no_milk": True, "entree": True, "price": 9.50},
        ]
    },
    {
        "name": "Mediterranean Mezze",
        "cuisine": "Mediterranean",
        "tags": "mediterranean,greek,healthy,lunch,dinner,vegetarian-friendly",
        "items": [
            {"name": "Falafel Wrap", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "high_protein": True, "entree": True, "price": 11.50},
            {"name": "Chicken Shawarma Plate", "chicken": True, "halal": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 15.00},
            {"name": "Hummus Sampler", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 9.00},
            {"name": "Greek Salad", "vegetarian": True, "gluten_free": True, "organic": True, "no_eggs": True, "price": 10.50},
            {"name": "Lamb Gyro", "beef": True, "halal": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 14.50},
            {"name": "Baba Ganoush", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 7.50},
            {"name": "Baklava", "vegetarian": True, "sweet": True, "price": 6.50},
        ]
    },
    {
        "name": "Big Burgers & BBQ",
        "cuisine": "American BBQ",
        "tags": "burgers,bbq,american,dinner,lunch,grill,meat",
        "items": [
            {"name": "Classic Cheeseburger", "beef": True, "entree": True, "high_protein": True, "price": 13.00},
            {"name": "Pulled Pork Sandwich", "pork": True, "entree": True, "high_protein": True, "price": 12.50},
            {"name": "BBQ Ribs", "pork": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 19.00},
            {"name": "Bacon Cheeseburger", "beef": True, "pork": True, "high_protein": True, "entree": True, "price": 14.50},
            {"name": "Grilled Chicken Sandwich", "chicken": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 12.00},
            {"name": "Onion Rings", "vegetarian": True, "no_eggs": True, "no_milk": True, "price": 6.50},
            {"name": "Coleslaw", "vegetarian": True, "gluten_free": True, "no_eggs": True, "price": 4.50},
        ]
    },
    {
        "name": "Pho King Vietnamese",
        "cuisine": "Vietnamese",
        "tags": "vietnamese,pho,noodles,asian,soup,healthy",
        "items": [
            {"name": "Beef Pho", "beef": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 13.50},
            {"name": "Chicken Pho", "chicken": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 12.50},
            {"name": "Vegetarian Pho", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 11.00},
            {"name": "Spring Rolls (Fresh)", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "price": 7.50},
            {"name": "Banh Mi Sandwich", "pork": True, "no_red_meat": True, "entree": True, "price": 10.00},
            {"name": "Grilled Pork Vermicelli", "pork": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 13.00},
            {"name": "Vietnamese Coffee", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "price": 5.00},
        ]
    },
    {
        "name": "Himalayan Curry House",
        "cuisine": "Indian",
        "tags": "indian,curry,spicy,vegetarian-friendly,dinner,halal",
        "items": [
            {"name": "Chicken Tikka Masala", "chicken": True, "halal": True, "no_pork_products": True, "gluten_free": True, "entree": True, "price": 15.50},
            {"name": "Palak Paneer", "vegetarian": True, "gluten_free": True, "no_eggs": True, "entree": True, "price": 13.00},
            {"name": "Chana Masala", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 12.00},
            {"name": "Lamb Vindaloo", "beef": True, "halal": True, "no_pork_products": True, "gluten_free": True, "entree": True, "price": 17.00},
            {"name": "Vegetable Samosas", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "price": 6.50},
            {"name": "Naan Bread", "vegetarian": True, "no_eggs": True, "price": 3.50},
            {"name": "Mango Lassi", "vegetarian": True, "gluten_free": True, "sweet": True, "no_eggs": True, "price": 5.50},
        ]
    },
    {
        "name": "Thai Basil Kitchen",
        "cuisine": "Thai",
        "tags": "thai,asian,spicy,noodles,curry,dinner",
        "items": [
            {"name": "Pad Thai", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "entree": True, "price": 13.50},
            {"name": "Green Curry Chicken", "chicken": True, "gluten_free": True, "no_pork_products": True, "entree": True, "price": 14.00},
            {"name": "Vegetable Pad See Ew", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 12.00},
            {"name": "Tom Yum Soup", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "price": 8.50},
            {"name": "Massaman Curry", "beef": True, "gluten_free": True, "entree": True, "price": 15.50},
            {"name": "Spring Rolls", "vegetarian": True, "vegan": True, "no_eggs": True, "no_milk": True, "price": 6.50},
            {"name": "Mango Sticky Rice", "vegetarian": True, "vegan": True, "gluten_free": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 7.00},
        ]
    },
    {
        "name": "Fresh Catch Seafood",
        "cuisine": "Seafood",
        "tags": "seafood,fish,dinner,lunch,fresh,ocean",
        "items": [
            {"name": "Grilled Salmon", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 22.00},
            {"name": "Fish & Chips", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "entree": True, "price": 16.50},
            {"name": "Shrimp Scampi", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 19.00},
            {"name": "Clam Chowder", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "price": 9.50},
            {"name": "Lobster Roll", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 24.00},
            {"name": "Crab Cakes", "pescetarian": True, "seafood": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "price": 18.00},
            {"name": "Caesar Salad", "pescetarian": True, "no_pork_products": True, "no_red_meat": True, "price": 10.00},
        ]
    },
    {
        "name": "Farm to Table Bistro",
        "cuisine": "Farm Fresh",
        "tags": "organic,farm-to-table,healthy,local,lunch,dinner,sustainable",
        "items": [
            {"name": "Grass-Fed Beef Burger", "beef": True, "organic": True, "locally_sourced": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 16.00},
            {"name": "Heirloom Tomato Salad", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "locally_sourced": True, "no_eggs": True, "no_milk": True, "price": 11.50},
            {"name": "Free Range Chicken", "chicken": True, "organic": True, "locally_sourced": True, "no_pork_products": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 19.00},
            {"name": "Seasonal Vegetable Bowl", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "locally_sourced": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 14.50},
            {"name": "Wild Mushroom Risotto", "vegetarian": True, "organic": True, "locally_sourced": True, "gluten_free": True, "no_eggs": True, "entree": True, "price": 17.00},
            {"name": "Farm Fresh Eggs Benedict", "vegetarian": True, "organic": True, "locally_sourced": True, "entree": True, "price": 13.50},
            {"name": "Apple Crisp", "vegetarian": True, "organic": True, "locally_sourced": True, "sweet": True, "price": 8.00},
        ]
    },
    {
        "name": "Keto Kitchen",
        "cuisine": "Keto/Low-Carb",
        "tags": "keto,low-carb,healthy,paleo,high-protein,lunch,dinner",
        "items": [
            {"name": "Cauliflower Pizza", "vegetarian": True, "keto": True, "low_carb": True, "gluten_free": True, "no_eggs": True, "entree": True, "price": 15.00},
            {"name": "Bunless Bacon Burger", "beef": True, "pork": True, "keto": True, "low_carb": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 14.50},
            {"name": "Zucchini Noodles Alfredo", "vegetarian": True, "keto": True, "low_carb": True, "gluten_free": True, "no_eggs": True, "entree": True, "price": 13.00},
            {"name": "Ribeye Steak", "beef": True, "keto": True, "low_carb": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 26.00},
            {"name": "Avocado Chicken Salad", "chicken": True, "keto": True, "low_carb": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "price": 12.50},
            {"name": "Cheese & Charcuterie", "keto": True, "low_carb": True, "gluten_free": True, "no_eggs": True, "price": 16.00},
            {"name": "Keto Cheesecake", "vegetarian": True, "keto": True, "low_carb": True, "gluten_free": True, "sweet": True, "low_sugar": True, "price": 7.50},
        ]
    },
    {
        "name": "Kosher Deli Express",
        "cuisine": "Jewish Deli",
        "tags": "kosher,deli,sandwiches,lunch,breakfast,jewish",
        "items": [
            {"name": "Pastrami on Rye", "beef": True, "kosher": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 14.00},
            {"name": "Matzo Ball Soup", "chicken": True, "kosher": True, "no_pork_products": True, "price": 8.50},
            {"name": "Lox & Bagel", "pescetarian": True, "seafood": True, "kosher": True, "no_pork_products": True, "no_red_meat": True, "entree": True, "price": 12.50},
            {"name": "Chicken Schnitzel", "chicken": True, "kosher": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 15.50},
            {"name": "Potato Latkes", "vegetarian": True, "kosher": True, "no_pork_products": True, "no_milk": True, "price": 7.00},
            {"name": "Israeli Salad", "vegetarian": True, "vegan": True, "gluten_free": True, "kosher": True, "no_eggs": True, "no_milk": True, "price": 6.50},
            {"name": "Rugelach", "vegetarian": True, "kosher": True, "sweet": True, "price": 5.50},
        ]
    },
    {
        "name": "Smoothie Bar & Juice",
        "cuisine": "Smoothies/Juice",
        "tags": "smoothies,juice,healthy,breakfast,vegan-friendly,organic",
        "items": [
            {"name": "Green Detox Smoothie", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "raw": True, "no_eggs": True, "no_milk": True, "price": 8.50},
            {"name": "Berry Blast Smoothie", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 7.50},
            {"name": "Protein Power Shake", "vegetarian": True, "gluten_free": True, "high_protein": True, "low_sugar": True, "no_eggs": True, "price": 9.00},
            {"name": "Fresh Orange Juice", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "raw": True, "no_eggs": True, "no_milk": True, "price": 6.00},
            {"name": "Acai Bowl", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 11.00},
            {"name": "Wheatgrass Shot", "vegetarian": True, "vegan": True, "gluten_free": True, "organic": True, "raw": True, "no_eggs": True, "no_milk": True, "price": 4.00},
            {"name": "Peanut Butter Banana", "vegetarian": True, "vegan": True, "gluten_free": True, "high_protein": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 8.00},
        ]
    },
    {
        "name": "Taco Truck Fiesta",
        "cuisine": "Mexican Street Food",
        "tags": "mexican,tacos,street-food,dinner,lunch,authentic",
        "items": [
            {"name": "Al Pastor Tacos", "pork": True, "gluten_free": True, "no_red_meat": True, "entree": True, "price": 11.00},
            {"name": "Carnitas Burrito", "pork": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 12.50},
            {"name": "Vegan Tacos", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "entree": True, "price": 9.50},
            {"name": "Elote (Street Corn)", "vegetarian": True, "gluten_free": True, "no_eggs": True, "price": 5.50},
            {"name": "Beef Barbacoa Tacos", "beef": True, "gluten_free": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 12.00},
            {"name": "Chips & Salsa", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 4.50},
            {"name": "Horchata", "vegetarian": True, "vegan": True, "gluten_free": True, "sweet": True, "no_eggs": True, "no_milk": True, "price": 4.00},
        ]
    },
    {
        "name": "Southern Fried Chicken",
        "cuisine": "Southern/Soul Food",
        "tags": "southern,chicken,comfort-food,dinner,lunch,fried",
        "items": [
            {"name": "Fried Chicken (3pc)", "chicken": True, "no_pork_products": True, "high_protein": True, "entree": True, "price": 13.50},
            {"name": "Chicken & Waffles", "chicken": True, "no_pork_products": True, "sweet": True, "entree": True, "price": 15.00},
            {"name": "Mac & Cheese", "vegetarian": True, "no_eggs": True, "price": 6.50},
            {"name": "Collard Greens", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 5.00},
            {"name": "Buttermilk Biscuits", "vegetarian": True, "no_eggs": True, "price": 4.00},
            {"name": "Pulled Pork BBQ", "pork": True, "gluten_free": True, "high_protein": True, "entree": True, "price": 14.00},
            {"name": "Peach Cobbler", "vegetarian": True, "sweet": True, "price": 7.00},
        ]
    },
    {
        "name": "Poke Paradise",
        "cuisine": "Hawaiian Poke",
        "tags": "poke,hawaiian,seafood,healthy,lunch,fresh,asian",
        "items": [
            {"name": "Ahi Tuna Poke Bowl", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 15.50},
            {"name": "Salmon Poke Bowl", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 14.50},
            {"name": "Tofu Poke Bowl", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "high_protein": True, "entree": True, "price": 12.00},
            {"name": "Spicy Tuna Bowl", "pescetarian": True, "seafood": True, "gluten_free": True, "no_pork_products": True, "no_red_meat": True, "high_protein": True, "entree": True, "price": 16.00},
            {"name": "Seaweed Salad", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 5.50},
            {"name": "Edamame", "vegetarian": True, "vegan": True, "gluten_free": True, "no_eggs": True, "no_milk": True, "price": 4.50},
            {"name": "Mochi Ice Cream", "vegetarian": True, "gluten_free": True, "sweet": True, "no_eggs": True, "price": 6.00},
        ]
    },
    {
        "name": "Crepe & Waffle House",
        "cuisine": "French Cafe",
        "tags": "crepes,waffles,french,breakfast,dessert,sweet,brunch",
        "items": [
            {"name": "Nutella Crepe", "vegetarian": True, "sweet": True, "price": 9.50},
            {"name": "Ham & Cheese Crepe", "pork": True, "no_red_meat": True, "entree": True, "price": 11.50},
            {"name": "Belgian Waffle", "vegetarian": True, "sweet": True, "price": 10.00},
            {"name": "Fresh Berry Crepe", "vegetarian": True, "organic": True, "sweet": True, "price": 10.50},
            {"name": "Savory Mushroom Crepe", "vegetarian": True, "no_eggs": True, "entree": True, "price": 12.00},
            {"name": "Chicken Caesar Crepe", "chicken": True, "no_pork_products": True, "entree": True, "price": 13.50},
            {"name": "Crème Brûlée", "vegetarian": True, "gluten_free": True, "sweet": True, "price": 8.50},
        ]
    },
]


# Street name variety
STREETS = [
    "Main Street", "Oak Avenue", "Maple Drive", "Pine Lane", "Cedar Court",
    "Elm Boulevard", "Birch Way", "Willow Road", "Aspen Circle", "Cherry Street",
    "Spruce Avenue", "Poplar Drive", "Redwood Lane", "Hickory Court", "Walnut Boulevard"
]


def seed_database():
    """Seed the database with realistic restaurant data."""
    print("Creating database tables...")
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()

    try:
        # Check if database is already seeded
        existing_vendors = db.query(Vendor).count()
        if existing_vendors > 0:
            print(f"Database already has {existing_vendors} vendors.")
            print("FORCE RESEED: Deleting existing data...")
            db.query(Item).delete()
            db.query(Vendor).delete()
            db.commit()
            print("Existing data cleared. Proceeding with fresh seed...")

        print("Seeding database with realistic restaurant data...")

        # Bozeman, MT location: (45.6770, -111.0429)
        bozeman_zips = [59715, 59718, 59771, 59772]

        for i, restaurant in enumerate(RESTAURANTS, start=1):
            # Distribute restaurants within 10-mile radius
            if i <= 7:
                distance_factor = random.uniform(0, 0.042)  # 0-3 miles
            elif i <= 14:
                distance_factor = random.uniform(0.042, 0.084)  # 3-6 miles
            else:
                distance_factor = random.uniform(0.084, 0.126)  # 6-9 miles

            angle = random.uniform(0, 2 * math.pi)
            lat_offset = distance_factor * random.choice([-1, 1]) * abs(math.cos(angle))
            lng_offset = distance_factor * random.choice([-1, 1]) * abs(math.sin(angle))

            # Create vendor
            vendor = Vendor(
                name=restaurant["name"],
                lat=45.6770 + lat_offset,
                lng=-111.0429 + lng_offset,
                address=f"{random.randint(100, 9999)} {random.choice(STREETS)}, Bozeman, MT",
                zipcode=bozeman_zips[i % len(bozeman_zips)],
                phone=f"406-{random.randint(200, 999)}-{random.randint(1000, 9999)}",
                website=f"https://www.{restaurant['name'].lower().replace(' ', '').replace('&', 'and')}.com",
                hours='{"monday": "11:00-22:00", "tuesday": "11:00-22:00", "wednesday": "11:00-22:00", "thursday": "11:00-23:00", "friday": "11:00-23:30", "saturday": "10:00-23:30", "sunday": "10:00-21:00"}',
                seo_tags=restaurant["tags"],
                region=i,
                custom_by_nature=(i % 3 == 0),
                delivery=random.choice([True, False]),
                takeout=True,
                grubhub=random.choice([True, False]),
                doordash=random.choice([True, False]),
                ubereats=random.choice([True, False]),
                postmates=random.choice([True, False]),
                yelp=True,
                google_reviews=True,
                tripadvisor=random.choice([True, False])
            )

            db.add(vendor)
            db.flush()

            # Create menu items for this vendor
            for item_data in restaurant["items"]:
                # Generate realistic ratings
                total_votes = random.randint(10, 100)
                upvotes = random.randint(int(total_votes * 0.5), total_votes)  # 50-100% approval

                item = Item(
                    vendor_id=vendor.id,
                    name=item_data["name"],
                    price=item_data["price"],
                    pictures="",
                    # Dietary preferences from item data
                    vegetarian=item_data.get("vegetarian", False),
                    pescetarian=item_data.get("pescetarian", False),
                    vegan=item_data.get("vegan", False),
                    keto=item_data.get("keto", False),
                    organic=item_data.get("organic", False),
                    gmo_free=item_data.get("gmo_free", False),
                    locally_sourced=item_data.get("locally_sourced", False),
                    raw=item_data.get("raw", False),
                    kosher=item_data.get("kosher", False),
                    halal=item_data.get("halal", False),
                    # Meat types
                    beef=item_data.get("beef", False),
                    chicken=item_data.get("chicken", False),
                    pork=item_data.get("pork", False),
                    seafood=item_data.get("seafood", False),
                    no_pork_products=item_data.get("no_pork_products", False),
                    no_red_meat=item_data.get("no_red_meat", False),
                    # Allergens
                    no_milk=item_data.get("no_milk", False),
                    no_eggs=item_data.get("no_eggs", False),
                    no_fish=item_data.get("no_fish", False),
                    no_shellfish=item_data.get("no_shellfish", False),
                    no_peanuts=item_data.get("no_peanuts", True),  # Default to no peanuts
                    no_treenuts=item_data.get("no_treenuts", True),  # Default to no tree nuts
                    gluten_free=item_data.get("gluten_free", False),
                    no_soy=item_data.get("no_soy", True),  # Default to no soy
                    no_sesame=item_data.get("no_sesame", True),  # Default to no sesame
                    no_msg=item_data.get("no_msg", True),  # Default to no MSG
                    no_alliums=item_data.get("no_alliums", False),
                    # Nutritional
                    low_sugar=item_data.get("low_sugar", False),
                    high_protein=item_data.get("high_protein", False),
                    low_carb=item_data.get("low_carb", False),
                    # Classification
                    entree=item_data.get("entree", False),
                    sweet=item_data.get("sweet", False),
                    # Rating
                    upvotes=upvotes,
                    total_votes=total_votes
                )

                db.add(item)

            if i % 5 == 0:
                print(f"Created {i} restaurants...")

        db.commit()
        print(f"✅ Successfully seeded database with {len(RESTAURANTS)} diverse restaurants!")
        print(f"   Total items: {sum(len(r['items']) for r in RESTAURANTS)}")

    except Exception as e:
        print(f"❌ Error seeding database: {e}")
        db.rollback()
        raise
    finally:
        db.close()


if __name__ == "__main__":
    seed_database()
