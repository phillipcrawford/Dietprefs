from sqlalchemy import Column, Integer, String, Float, Boolean, Text, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Vendor(Base):
    """Vendor/Restaurant model representing food establishments."""

    __tablename__ = "vendors"

    # Primary key
    id = Column(Integer, primary_key=True, index=True)

    # Basic info
    name = Column(String(255), nullable=False, index=True)
    address = Column(Text)
    zipcode = Column(Integer)
    phone = Column(String(20))
    website = Column(Text)
    hours = Column(Text)  # JSON string: {"monday": "10:00-22:00", ...}
    seo_tags = Column(Text)  # Comma-separated tags

    # Location
    lat = Column(Float, nullable=False, index=True)
    lng = Column(Float, nullable=False, index=True)
    region = Column(Integer)

    # Delivery options
    delivery = Column(Boolean, default=False)
    takeout = Column(Boolean, default=False)
    grubhub = Column(Boolean, default=False)
    doordash = Column(Boolean, default=False)
    ubereats = Column(Boolean, default=False)
    postmates = Column(Boolean, default=False)

    # Review platforms
    yelp = Column(Boolean, default=False)
    google_reviews = Column(Boolean, default=False)
    tripadvisor = Column(Boolean, default=False)

    # Special attributes
    custom_by_nature = Column(Boolean, default=False)

    # Timestamps
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    # Relationships
    items = relationship("Item", back_populates="vendor", cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Vendor(id={self.id}, name='{self.name}')>"
