-- CineTrack schema — esegui su Neon Console (SQL Editor)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now()
);

-- stato tracking: watchlist | watching | watched | favorite | dropped
CREATE TABLE IF NOT EXISTS watchlist_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  tmdb_id INT NOT NULL,
  media_type TEXT NOT NULL CHECK (media_type IN ('movie','tv')),
  status TEXT NOT NULL DEFAULT 'watchlist' CHECK (status IN ('watchlist','watching','watched','favorite','dropped')),
  rating INT CHECK (rating IS NULL OR (rating >= 1 AND rating <= 10)),
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, tmdb_id, media_type)
);
CREATE INDEX IF NOT EXISTS idx_watchlist_user ON watchlist_items(user_id);

CREATE TABLE IF NOT EXISTS episode_progress (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  tv_tmdb_id INT NOT NULL,
  season_number INT NOT NULL,
  episode_number INT NOT NULL,
  watched BOOLEAN NOT NULL DEFAULT true,
  watched_at TIMESTAMPTZ DEFAULT now(),
  UNIQUE(user_id, tv_tmdb_id, season_number, episode_number)
);
CREATE INDEX IF NOT EXISTS idx_episodes_user_tv ON episode_progress(user_id, tv_tmdb_id);
