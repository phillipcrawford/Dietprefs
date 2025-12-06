from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.database import engine, Base
from app.api.v1 import vendors, items, admin, config

# Create database tables
Base.metadata.create_all(bind=engine)

# Initialize FastAPI app
app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Backend API for Dietprefs food vendor discovery app"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Health check endpoints
@app.get("/")
async def root():
    """Root endpoint for health checks."""
    return {
        "message": "Dietprefs API is running",
        "version": settings.VERSION,
        "environment": settings.ENVIRONMENT
    }


@app.get("/health")
async def health_check():
    """Health check endpoint for monitoring."""
    return {"status": "healthy"}


@app.post("/seed")
async def seed_database_endpoint():
    """
    Seed the database with sample restaurant data.
    This will clear existing data and repopulate with fresh seed data.
    Can be called multiple times to refresh the database.
    """
    try:
        from app.seed import seed_database
        seed_database()
        return {
            "status": "success",
            "message": "Database seeded successfully with 20 restaurants"
        }
    except Exception as e:
        return {
            "status": "error",
            "message": f"Failed to seed database: {str(e)}"
        }


# Include API routers
app.include_router(config.router, prefix=settings.API_V1_PREFIX, tags=["config"])
app.include_router(vendors.router, prefix=settings.API_V1_PREFIX, tags=["vendors"])
app.include_router(items.router, prefix=settings.API_V1_PREFIX, tags=["items"])
app.include_router(admin.router, prefix=settings.API_V1_PREFIX, tags=["admin"])
