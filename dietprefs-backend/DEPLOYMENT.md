# Dietprefs Backend Deployment Guide

## Local Development Setup

### Prerequisites
- Python 3.11 or higher
- PostgreSQL 15 or higher
- Git

### Quick Start (Local Development)

1. **Clone and navigate to backend directory**:
```bash
cd dietprefs-backend
```

2. **Create virtual environment**:
```bash
python -m venv venv

# On macOS/Linux:
source venv/bin/activate

# On Windows:
venv\Scripts\activate
```

3. **Install dependencies**:
```bash
pip install -r requirements.txt
```

4. **Set up PostgreSQL database**:
```bash
# Create database (using psql or your preferred method)
createdb dietprefs

# Or with psql:
psql -U postgres
CREATE DATABASE dietprefs;
\q
```

5. **Configure environment variables**:
```bash
cp .env.example .env
# Edit .env with your database credentials:
# DATABASE_URL=postgresql://username:password@localhost:5432/dietprefs
```

6. **Run database migrations** (creates tables):
```bash
alembic upgrade head
```

7. **Seed database with test data**:
```bash
python -m app.seed
```

8. **Start development server**:
```bash
uvicorn app.main:app --reload
```

9. **Test the API**:
- API: http://localhost:8000
- Interactive docs: http://localhost:8000/docs
- Alternative docs: http://localhost:8000/redoc

### Testing the API

**Test search endpoint** (without preferences):
```bash
curl -X POST http://localhost:8000/api/v1/vendors/search \
  -H "Content-Type: application/json" \
  -d '{
    "user1_preferences": [],
    "user2_preferences": [],
    "page": 1,
    "page_size": 10
  }'
```

**Test with dietary preferences**:
```bash
curl -X POST http://localhost:8000/api/v1/vendors/search \
  -H "Content-Type: application/json" \
  -d '{
    "user1_preferences": ["vegetarian", "gluten_free"],
    "user2_preferences": ["keto"],
    "lat": 37.7749,
    "lng": -122.4194,
    "sort_by": "rating",
    "sort_direction": "desc",
    "page": 1,
    "page_size": 10
  }'
```

---

## Railway Deployment

### Step 1: Prepare for Deployment

1. **Create a Railway account**: https://railway.app
2. **Install Railway CLI** (optional):
```bash
npm install -g @railway/cli
```

### Step 2: Deploy via Railway Dashboard

1. Go to https://railway.app/new
2. Click "Deploy from GitHub repo"
3. Select your `dietprefs-backend` repository
4. Railway will auto-detect it's a Python app

### Step 3: Add PostgreSQL Database

1. In your Railway project, click "New"
2. Select "Database" → "PostgreSQL"
3. Railway will create a database and set `DATABASE_URL` automatically

### Step 4: Configure Environment Variables

In Railway project settings, add:
```
ENVIRONMENT=production
ALLOWED_ORIGINS=https://yourapp.com,dietprefs://
```

`DATABASE_URL` is automatically set by Railway.

### Step 5: Deploy

Railway will automatically deploy on git push. Monitor logs in the Railway dashboard.

### Step 6: Run Seed Script (One-Time)

After first deployment, seed the database:

**Option A: Via Railway CLI**:
```bash
railway run python -m app.seed
```

**Option B: Via Railway Dashboard**:
1. Go to your service
2. Click "Settings" → "Deploy"
3. Add seed command to deploy script

---

## Render Deployment (Alternative)

### Step 1: Create Render Account
Go to https://render.com and sign up.

### Step 2: Create PostgreSQL Database
1. Click "New +" → "PostgreSQL"
2. Name: `dietprefs-db`
3. Note the "Internal Database URL" after creation

### Step 3: Create Web Service
1. Click "New +" → "Web Service"
2. Connect your GitHub repository
3. Configure:
   - **Name**: `dietprefs-api`
   - **Environment**: Python 3
   - **Build Command**: `pip install -r requirements.txt`
   - **Start Command**: `uvicorn app.main:app --host 0.0.0.0 --port $PORT`

### Step 4: Add Environment Variables
In your web service settings, add:
```
DATABASE_URL=[your internal database URL from step 2]
ENVIRONMENT=production
ALLOWED_ORIGINS=https://yourapp.com,dietprefs://
```

### Step 5: Deploy
Render will automatically deploy. Check logs for any errors.

### Step 6: Seed Database
Use Render Shell (available in dashboard) to run:
```bash
python -m app.seed
```

---

## Heroku Deployment (Alternative)

### Prerequisites
- Heroku CLI installed
- Heroku account

### Steps

1. **Login to Heroku**:
```bash
heroku login
```

2. **Create Heroku app**:
```bash
heroku create dietprefs-api
```

3. **Add PostgreSQL**:
```bash
heroku addons:create heroku-postgresql:mini
```

4. **Set environment variables**:
```bash
heroku config:set ENVIRONMENT=production
heroku config:set ALLOWED_ORIGINS=https://yourapp.com,dietprefs://
```

5. **Create Procfile**:
```
web: uvicorn app.main:app --host 0.0.0.0 --port $PORT
```

6. **Deploy**:
```bash
git push heroku main
```

7. **Seed database**:
```bash
heroku run python -m app.seed
```

8. **View logs**:
```bash
heroku logs --tail
```

---

## Production Checklist

- [ ] Set `ENVIRONMENT=production` in environment variables
- [ ] Configure `ALLOWED_ORIGINS` with your actual domains
- [ ] Database is using SSL/TLS connection
- [ ] Seed database with initial data
- [ ] Test all API endpoints
- [ ] Set up monitoring/logging
- [ ] Configure rate limiting (if needed)
- [ ] Set up automated backups for database

---

## Troubleshooting

### "Connection refused" errors
- Check DATABASE_URL is correct
- Ensure PostgreSQL is running
- Verify database exists

### "Module not found" errors
- Ensure virtual environment is activated
- Run `pip install -r requirements.txt`

### Migrations fail
- Check database connection
- Try: `alembic revision --autogenerate -m "initial"`
- Then: `alembic upgrade head`

### API returns 500 errors
- Check logs: `uvicorn app.main:app --reload` (shows detailed errors)
- Verify all environment variables are set
- Check database has been seeded

---

## Monitoring & Maintenance

### Check API health:
```bash
curl https://your-api-url.com/health
```

### View logs (Railway):
```bash
railway logs
```

### View logs (Render):
Available in dashboard under "Logs"

### View logs (Heroku):
```bash
heroku logs --tail
```

### Database backups:
- Railway: Automatic backups on Pro plan
- Render: Automatic backups included
- Heroku: Use `heroku pg:backups:capture`

---

## Next Steps

After backend is deployed:
1. Note your API URL (e.g., `https://dietprefs-api.up.railway.app`)
2. Update Android app to use this URL
3. Test API endpoints from Android app
4. Monitor logs for any issues

See [architecture.md](../architecture.md) for more details on the overall system architecture.
