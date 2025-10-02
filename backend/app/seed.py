"""
Database seeding script for Dietprefs.
Creates 20 vendors with 7 menu items each for testing.
"""
import random
import math
from app.database import SessionLocal, engine, Base
from app.models.vendor import Vendor
from app.models.item import Item


def seed_database():
    """Seed the database with test vendors and items."""
    print("Creating database tables...")
    Base.metadata.create_all(bind=engine)

    db = SessionLocal()

    try:
        # Check if database is already seeded
        existing_vendors = db.query(Vendor).count()
        if existing_vendors > 0:
            print(f"Database already has {existing_vendors} vendors.")
            print("FORCE RESEED: Deleting existing data...")
            # Delete all existing data
            db.query(Item).delete()
            db.query(Vendor).delete()
            db.commit()
            print("Existing data cleared. Proceeding with fresh seed...")

        print("Seeding database with test data...")

        # Test location: San Francisco (37.7749, -122.4194)
        # Spread vendors across different distances to test 10-mile filter
        # ~0.014 degrees latitude/longitude ≈ 1 mile
        for i in range(1, 21):  # 20 vendors
            # Vendors 1-10: Within 5 miles (should appear in results)
            # Vendors 11-15: 5-10 miles (should appear in results)
            # Vendors 16-20: Beyond 10 miles (should be filtered out)
            if i <= 10:
                # Within 5 miles: 0-5 mile radius
                distance_factor = random.uniform(0, 0.07)
            elif i <= 15:
                # 5-10 miles: 5-10 mile radius
                distance_factor = random.uniform(0.07, 0.14)
            else:
                # Beyond 10 miles: 10-15 mile radius (for testing filter)
                distance_factor = random.uniform(0.14, 0.21)

            # Random angle for spreading vendors around the center
            angle = random.uniform(0, 2 * 3.14159)
            lat_offset = distance_factor * random.choice([-1, 1]) * abs(math.cos(angle))
            lng_offset = distance_factor * random.choice([-1, 1]) * abs(math.sin(angle))

            vendor = Vendor(
                name=f"Vendor {i}",
                lat=37.7749 + lat_offset,  # San Francisco center
                lng=-122.4194 + lng_offset,
                address=f"{100 + i * 10} Main St, San Francisco, CA",
                zipcode=94100 + i,
                phone=f"415555{i:04d}",
                website=f"https://vendor{i}.example.com",
                hours='{"monday": "10:00-22:00", "tuesday": "10:00-22:00"}',
                seo_tags="food,restaurants,dining",
                region=i,
                custom_by_nature=(i % 2 == 0),
                delivery=(i % 3 == 0),
                takeout=(i % 7 == 0),
                grubhub=(i % 5 == 0),
                doordash=(i % 4 == 0),
                ubereats=(i % 9 == 0),
                postmates=(i % 6 == 0),
                yelp=(i % 10 == 0),
                google_reviews=(i % 2 != 0),
                tripadvisor=(i % 8 == 0)
            )

            db.add(vendor)
            db.flush()  # Get the vendor ID

            # Create 7 items for each vendor with VARIED dietary patterns per vendor
            # Use vendor ID to create different restaurant "specialties"
            for j in range(1, 8):
                total_votes = random.randint(5, 51)
                upvotes = random.randint(0, total_votes + 1)

                # Each vendor has different probabilities for dietary options
                # This makes some vendors more vegan-friendly, others more pescetarian, etc.
                is_veg_focused = (i % 5 == 0)  # Every 5th vendor is vegetarian-focused
                is_vegan_focused = (i % 7 == 0)  # Every 7th vendor is vegan-focused
                is_seafood_focused = (i % 4 == 0)  # Every 4th vendor has more seafood

                item = Item(
                    vendor_id=vendor.id,
                    name=f"Item {j} from Vendor {i}",
                    price=round(random.uniform(5.0, 25.0), 2),
                    pictures="",

                    # Dietary preferences - varied by vendor type
                    vegetarian=(is_veg_focused and j <= 5) or (j % 3 == 0),
                    pescetarian=(is_seafood_focused and j <= 4) or (j % 2 == 0),
                    vegan=(is_vegan_focused and j <= 3) or (j % 4 == 0),
                    keto=(j % 5 == 0),
                    organic=(j % 2 != 0),
                    gmo_free=(j == 1),
                    locally_sourced=(j == 2),
                    raw=(j == 7),
                    kosher=True,
                    halal=(j % 6 == 0),

                    # Meat types
                    beef=(j == 3),
                    chicken=(j == 1),
                    pork=(j == 4),
                    seafood=(j == 2),
                    no_pork_products=(j != 4),
                    no_red_meat=(j not in [1, 3]),

                    # Allergens
                    no_milk=(j % 3 != 0),
                    no_eggs=(j % 4 != 0),
                    no_fish=(j != 2),
                    no_shellfish=(j != 2),
                    no_peanuts=(j % 5 != 0),
                    no_treenuts=(j % 6 != 0),
                    gluten_free=(j % 3 != 0),
                    no_soy=(j % 4 != 0),
                    no_sesame=(j % 5 != 0),
                    no_msg=True,
                    no_alliums=(j == 7),

                    # Nutritional
                    low_sugar=(j % 3 == 0),
                    high_protein=True,
                    low_carb=(j % 2 == 0),

                    # Classification
                    entree=(j in [1, 2]),
                    sweet=(j == 5),

                    # Rating
                    upvotes=upvotes,
                    total_votes=total_votes
                )

                db.add(item)

            if i % 5 == 0:
                print(f"Created {i} vendors...")

        db.commit()
        print(f"✅ Successfully seeded database with 20 vendors and 140 items!")

    except Exception as e:
        print(f"❌ Error seeding database: {e}")
        db.rollback()
        raise
    finally:
        db.close()


if __name__ == "__main__":
    seed_database()
