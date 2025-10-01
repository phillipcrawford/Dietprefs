from sqlalchemy import Column, Integer, String, Float, Boolean, Text, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Item(Base):
    """Menu item model with extensive dietary and allergen flags."""

    __tablename__ = "items"

    # Primary key
    id = Column(Integer, primary_key=True, index=True)

    # Foreign key to vendor
    vendor_id = Column(Integer, ForeignKey("vendors.id", ondelete="CASCADE"), nullable=False, index=True)

    # Basic info
    name = Column(String(255), nullable=False)
    price = Column(Float)
    pictures = Column(Text)  # Comma-separated URLs

    # Dietary preferences
    vegetarian = Column(Boolean, default=False, index=True)
    pescetarian = Column(Boolean, default=False)
    vegan = Column(Boolean, default=False, index=True)
    keto = Column(Boolean, default=False, index=True)
    organic = Column(Boolean, default=False)
    gmo_free = Column(Boolean, default=False)
    locally_sourced = Column(Boolean, default=False)
    raw = Column(Boolean, default=False)
    kosher = Column(Boolean, default=False)
    halal = Column(Boolean, default=False)

    # Meat types
    beef = Column(Boolean, default=False)
    chicken = Column(Boolean, default=False)
    pork = Column(Boolean, default=False)
    seafood = Column(Boolean, default=False)
    no_pork_products = Column(Boolean, default=False)
    no_red_meat = Column(Boolean, default=False)

    # Allergens
    no_milk = Column(Boolean, default=False)
    no_eggs = Column(Boolean, default=False)
    no_fish = Column(Boolean, default=False)
    no_shellfish = Column(Boolean, default=False)
    no_peanuts = Column(Boolean, default=False)
    no_treenuts = Column(Boolean, default=False)
    gluten_free = Column(Boolean, default=False, index=True)
    no_soy = Column(Boolean, default=False)
    no_sesame = Column(Boolean, default=False)
    no_msg = Column(Boolean, default=False)
    no_alliums = Column(Boolean, default=False)

    # Nutritional
    low_sugar = Column(Boolean, default=False)
    high_protein = Column(Boolean, default=False)
    low_carb = Column(Boolean, default=False)

    # Classification
    entree = Column(Boolean, default=False)
    sweet = Column(Boolean, default=False)

    # Rating system
    upvotes = Column(Integer, default=0)
    total_votes = Column(Integer, default=0)

    # Timestamps
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    # Relationships
    vendor = relationship("Vendor", back_populates="items")

    def __repr__(self):
        return f"<Item(id={self.id}, name='{self.name}', vendor_id={self.vendor_id})>"

    @property
    def rating_percentage(self) -> float:
        """Calculate rating as percentage (0.0 to 1.0)."""
        if self.total_votes == 0:
            return 0.0
        return self.upvotes / self.total_votes
