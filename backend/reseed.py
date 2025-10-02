"""Drop all data and reseed the database with new pattern."""
from app.database import SessionLocal, engine, Base
from app.models.vendor import Vendor
from app.models.item import Item

db = SessionLocal()

# Drop all tables and recreate
print("Dropping all tables...")
Base.metadata.drop_all(bind=engine)
print("Creating tables...")
Base.metadata.create_all(bind=engine)

# Now run seed
from app.seed import seed_database
print("Seeding database...")
seed_database()
print("Done! Database reseeded with new varied patterns.")
