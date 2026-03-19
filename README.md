# NAVTEX Monitor

Live maritime safety message aggregator for Turkey, Greece, United Kingdom and Sweden.

## Sources
| Country | Source | Station |
|---|---|---|
| 🇹🇷 Turkey | kiyiemniyeti.gov.tr | Istanbul, Antalya, Samsun, Izmir |
| 🇬🇷 Greece | hnhs.gr | Irakleio, Kerkyra, Limnos |
| 🇬🇧 United Kingdom | msi.admiralty.co.uk | NAVAREA I, UK Coastal |
| 🇸🇪 Sweden | navvarn.sjofartsverket.se | Swedish Maritime Administration |

## Project Structure
```
navtex-app/
├── backend/    → Spring Boot (Java 17) — scraping + REST API
└── frontend/   → React + Vite — dashboard UI
```

## Local Development

**Backend** (requires Java 17 + Maven):
```bash
cd backend
mvn spring-boot:run
```

**Frontend** (requires Node.js):
```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

## Deploy

### Backend → Render.com (free)
1. New Web Service → connect GitHub repo
2. Root Directory: `backend`
3. Build: `mvn clean package -DskipTests`
4. Start: `java -jar target/navtex-backend-1.0.0.jar`
5. Copy the Render URL (e.g. `https://navtex-backend.onrender.com`)

### Frontend → Vercel (free)
1. New Project → connect GitHub repo
2. Root Directory: `frontend`
3. Add Environment Variable: `VITE_API_URL` = your Render URL + `/api`
4. Update `frontend/vercel.json` with your Render URL
5. Deploy → get public link

## API Endpoints
- `GET /api/health` — health check
- `GET /api/messages` — all messages
- `GET /api/messages/country/{country}` — by country
