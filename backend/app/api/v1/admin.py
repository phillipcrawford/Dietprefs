from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.database import get_db, engine, Base
from app.seed import seed_database

router = APIRouter()


@router.post("/admin/reseed")
async def reseed_database(db: Session = Depends(get_db)):
    """
    ADMIN ONLY: Drop all data and reseed database.
    WARNING: This will delete all existing data!
    """
    # Drop all tables
    Base.metadata.drop_all(bind=engine)

    # Recreate tables
    Base.metadata.create_all(bind=engine)

    # Seed with new data
    seed_database()

    return {"message": "Database reseeded successfully with new varied patterns"}
