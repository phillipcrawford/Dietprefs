from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Database
    DATABASE_URL: str = "postgresql://user:password@localhost:5432/dietprefs"

    # API
    API_V1_PREFIX: str = "/api/v1"
    PROJECT_NAME: str = "Dietprefs API"
    VERSION: str = "1.0.0"

    # CORS
    ALLOWED_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:8080",
        "dietprefs://"
    ]

    # Environment
    ENVIRONMENT: str = "development"

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
