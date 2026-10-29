# CineTrack — tracking film / serie / anime

App Android nativa (Kotlin + Jetpack Compose + Material 3) + backend sync self-hosted.

## Stack scelto (dalle tue risposte)
- **Catalogo:** TMDB (film + serie TV + anime — TMDB contiene anche molti anime)
- **Server:** Render (hosting backend) + Neon Console (Postgres) + Resend (email verifica / reset password)
- **App:** Kotlin + Compose, Retrofit + Room + DataStore

## Struttura
```
backend/   → Node.js + Express API, deployabile su Render, DB Neon, email Resend
android/   → App Android nativa CineTrack
```

## Quick start
1. Backend: leggi `backend/README.md` → crea DB con `schema.sql`, setta `.env`, `npm install && npm run dev`
2. Android: apri `android/` in Android Studio, metti `TMDB_API_KEY` + `API_BASE_URL` in `local.properties`, run.

## Funzioni v1 (MVP)
- Auth email/password (JWT) + sync multi-dispositivo
- Ricerca TMDB, dettaglio film/serie/anime
- Watchlist / Watched / Preferiti
- Tracking episodi S/E con progressi
