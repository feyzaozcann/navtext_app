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

# NAVTEX Monitor

Live Demo: [https://senin-vercel-linkin.vercel.app](https://senin-vercel-linkin.vercel.app)

This project's frontend is deployed on Vercel and the backend is running on Render.

## API Endpoints
- `GET /api/health` — health check
- `GET /api/messages` — all messages
- `GET /api/messages/country/{country}` — by country
