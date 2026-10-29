# CineTrack Android

Kotlin + Jetpack Compose + Material 3. Nativa.

## Aprire il progetto
1. Apri **Android Studio** → Open → seleziona cartella `android/`
2. Crea `local.properties`:
```properties
sdk.dir=/Users/tuo-nome/Library/Android/sdk
```
3. In `app/build.gradle.kts` cambia `API_BASE_URL` con l'URL del tuo backend Render, es:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://cinetrack-backend.onrender.com/\"")
```
Per test locale con emulatore: `"http://10.0.2.2:3000/"`
4. Sync Gradle + Run ▶️

## Schermate
- Auth (login/registrazione JWT, token in DataStore)
- Home (trending TMDB via backend), Cerca (live), Libreria (filtri watchlist/watching/watched/favorite)
- Dettaglio film/serie + bottoni tracking, lista stagioni
- Stagione: checklist episodi con progressbar, sync su `/progress`

## Dipendenze
Retrofit + OkHttp, Coil (poster), Navigation-Compose, DataStore, Room (pronto per cache offline — da collegare), Coroutines.

## Nota anime
TMDB include gli anime (cerca titolo giapponese/inglese). Il backend usa `/search/multi` quindi trova movie+tv insieme.
