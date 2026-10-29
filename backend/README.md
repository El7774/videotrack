# CineTrack backend

Node + Express + Postgres (Neon) + Resend. Deploy su Render.

## 1. DB (Neon Console)
1. Crea progetto + database `cinetrack`
2. Apri SQL Editor, incolla ed esegui `schema.sql`
3. Copia la `DATABASE_URL` (con `?sslmode=require`)

## 2. TMDB
- Crea account su themoviedb.org → Settings → API → copia **API Key v3** oppure **Bearer v4**
- Anime: TMDB li contiene (es. cerca "Attack on Titan"), quindi una sola API basta per film+serie+anime.

## 3. Resend
- Crea API key su resend.com → verifica un dominio o usa `onboarding@resend.dev` per i test.

## 4. Locale
```bash
cd backend
cp .env.example .env   # compila i valori
npm install
npm run dev            # http://localhost:3000/health
```

## 5. Deploy su Render
- New → Web Service → collega repo → Root Directory `backend`
- Build `npm install`, Start `npm start`
- Env vars: DATABASE_URL, JWT_SECRET, TMDB_API_KEY (o TMDB_BEARER), RESEND_API_KEY
- Oppure usa `render.yaml` (Blueprint).

## Endpoints
- POST /auth/register, POST /auth/login, GET /me
- GET /search?q=, GET /trending, GET /movie/:id, GET /tv/:id, GET /tv/:id/season/:n
- GET /list, PUT /list, DELETE /list/:media/:tmdbId
- GET /progress/:tvId, PUT /progress
Tutti (tranne /health e /auth/*) richiedono `Authorization: Bearer <JWT>`.
