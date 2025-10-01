# Dietprefs Backend API

FastAPI backend for Dietprefs food vendor discovery app.

## Tech Stack
- **Framework**: FastAPI
- **Database**: PostgreSQL
- **ORM**: SQLAlchemy
- **Migrations**: Alembic
- **Deployment**: Railway

## Setup

### Prerequisites
- Python 3.11+
- PostgreSQL 15+

### Local Development

1. Create virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Set up environment variables:
```bash
cp .env.example .env
# Edit .env with your database credentials
```

4. Run database migrations:
```bash
alembic upgrade head
```

5. Seed database (optional):
```bash
python -m app.seed
```

6. Start development server:
```bash
uvicorn app.main:app --reload
```

API will be available at: http://localhost:8000
API docs at: http://localhost:8000/docs

## Project Structure
```
dietprefs-backend/
├── app/
│   ├── main.py              # FastAPI app entry point
│   ├── config.py            # Configuration & settings
│   ├── database.py          # Database connection
│   ├── models/              # SQLAlchemy models
│   │   ├── vendor.py
│   │   └── item.py
│   ├── schemas/             # Pydantic schemas (request/response)
│   │   ├── vendor.py
│   │   └── item.py
│   ├── api/                 # API routes
│   │   └── v1/
│   │       ├── vendors.py
│   │       └── items.py
│   ├── services/            # Business logic
│   │   └── vendor_service.py
│   └── seed.py              # Database seeding script
├── alembic/                 # Database migrations
├── tests/                   # Test files
├── requirements.txt
├── .env.example
└── README.md
```

## API Endpoints

### Vendor Endpoints
- `POST /api/v1/vendors/search` - Search vendors by preferences
- `GET /api/v1/vendors/{id}` - Get vendor details
- `GET /api/v1/vendors/{id}/items` - Get vendor menu items

### Item Endpoints
- `POST /api/v1/items/{id}/vote` - Vote on item

## Deployment

Deploy to Railway:
```bash
railway login
railway init
railway up
```

See [architecture.md](../architecture.md) for detailed deployment instructions.
