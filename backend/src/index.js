import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import bcrypt from 'bcryptjs';
import { Resend } from 'resend';
import { pool } from './db.js';
import { signToken, authMiddleware } from './auth.js';
import { tmdbGet } from './tmdb.js';

dotenv.config();
const app = express();
app.use(cors());
app.use(express.json());

const resend = process.env.RESEND_API_KEY ? new Resend(process.env.RESEND_API_KEY) : null;

// ---------- health ----------
app.get('/health', (req, res) => res.json({ ok: true }));

// ---------- auth ----------
app.post('/auth/register', async (req, res) => {
  const { email, password } = req.body ?? {};
  if (!email || !password || password.length < 8)
    return res.status(400).json({ error: 'email e password (min 8 char) obbligatorie' });
  try {
    const hash = await bcrypt.hash(password, 10);
    const { rows } = await pool.query(
      'INSERT INTO users(email, password_hash) VALUES(lower($1), $2) RETURNING id, email',
      [email, hash]
    );
    // email benvenuto (best-effort, non blocca)
    if (resend) {
      resend.emails.send({
        from: 'CineTrack <onboarding@resend.dev>',
        to: email,
        subject: 'Benvenuto su CineTrack 🎬',
        html: '<p>Ciao! Il tuo account CineTrack è attivo. Buona visione!</p>'
      }).catch(() => {});
    }
    return res.json({ token: signToken(rows[0].id), user: rows[0] });
  } catch (e) {
    if (e.code === '23505') return res.status(409).json({ error: 'email già registrata' });
    console.error(e);
    return res.status(500).json({ error: 'db error' });
  }
});

app.post('/auth/login', async (req, res) => {
  const { email, password } = req.body ?? {};
  const { rows } = await pool.query('SELECT * FROM users WHERE email = lower($1)', [email]);
  const u = rows[0];
  if (!u || !(await bcrypt.compare(password, u.password_hash)))
    return res.status(401).json({ error: 'credenziali non valide' });
  return res.json({ token: signToken(u.id), user: { id: u.id, email: u.email } });
});

app.get('/me', authMiddleware, async (req, res) => {
  const { rows } = await pool.query('SELECT id, email, created_at FROM users WHERE id=$1', [req.user.sub]);
  res.json(rows[0] ?? null);
});

// ---------- catalogo (via TMDB, con chiave server) ----------
app.get('/search', authMiddleware, async (req, res) => {
  try {
    const q = req.query.q ?? '';
    if (!q) return res.json({ results: [] });
    const data = await tmdbGet('/search/multi', { query: q, include_adult: 'false', page: req.query.page ?? '1' });
    res.json(data);
  } catch (e) { res.status(502).json({ error: 'tmdb error' }); }
});

app.get('/movie/:id', authMiddleware, async (req, res) => {
  try { res.json(await tmdbGet(`/movie/${req.params.id}`)); }
  catch { res.status(502).json({ error: 'tmdb error' }); }
});

app.get('/tv/:id', authMiddleware, async (req, res) => {
  try { res.json(await tmdbGet(`/tv/${req.params.id}`)); }
  catch { res.status(502).json({ error: 'tmdb error' }); }
});

app.get('/tv/:id/season/:n', authMiddleware, async (req, res) => {
  try { res.json(await tmdbGet(`/tv/${req.params.id}/season/${req.params.n}`)); }
  catch { res.status(502).json({ error: 'tmdb error' }); }
});

app.get('/trending', authMiddleware, async (req, res) => {
  try { res.json(await tmdbGet('/trending/all/week')); }
  catch { res.status(502).json({ error: 'tmdb error' }); }
});

// ---------- tracking ----------
app.get('/list', authMiddleware, async (req, res) => {
  const { rows } = await pool.query(
    'SELECT * FROM watchlist_items WHERE user_id=$1 ORDER BY updated_at DESC', [req.user.sub]
  );
  res.json(rows);
});

app.put('/list', authMiddleware, async (req, res) => {
  const { tmdb_id, media_type, status, rating } = req.body ?? {};
  if (!tmdb_id || !['movie','tv'].includes(media_type))
    return res.status(400).json({ error: 'tmdb_id e media_type movie|tv obbligatori' });
  const { rows } = await pool.query(
    `INSERT INTO watchlist_items(user_id, tmdb_id, media_type, status, rating)
     VALUES($1,$2,$3,COALESCE($4,'watchlist'),$5)
     ON CONFLICT(user_id, tmdb_id, media_type)
     DO UPDATE SET status=COALESCE($4, watchlist_items.status),
                   rating=$5, updated_at=now()
     RETURNING *`,
    [req.user.sub, tmdb_id, media_type, status ?? null, rating ?? null]
  );
  res.json(rows[0]);
});

app.delete('/list/:media/:tmdbId', authMiddleware, async (req, res) => {
  await pool.query('DELETE FROM watchlist_items WHERE user_id=$1 AND media_type=$2 AND tmdb_id=$3',
    [req.user.sub, req.params.media, req.params.tmdbId]);
  res.json({ ok: true });
});

// progressi episodi
app.get('/progress/:tvId', authMiddleware, async (req, res) => {
  const { rows } = await pool.query(
    'SELECT season_number, episode_number, watched, watched_at FROM episode_progress WHERE user_id=$1 AND tv_tmdb_id=$2 ORDER BY season_number, episode_number',
    [req.user.sub, req.params.tvId]
  );
  res.json(rows);
});

app.put('/progress', authMiddleware, async (req, res) => {
  const { tv_tmdb_id, season_number, episode_number, watched } = req.body ?? {};
  if (tv_tmdb_id == null || season_number == null || episode_number == null)
    return res.status(400).json({ error: 'tv_tmdb_id, season_number, episode_number obbligatori' });
  const { rows } = await pool.query(
    `INSERT INTO episode_progress(user_id, tv_tmdb_id, season_number, episode_number, watched)
     VALUES($1,$2,$3,$4,COALESCE($5,true))
     ON CONFLICT(user_id, tv_tmdb_id, season_number, episode_number)
     DO UPDATE SET watched=COALESCE($5,true), watched_at=now()
     RETURNING *`,
    [req.user.sub, tv_tmdb_id, season_number, episode_number, watched ?? true]
  );
  // se segno episodi, metto la serie in "watching" automaticamente
  await pool.query(
    `INSERT INTO watchlist_items(user_id, tmdb_id, media_type, status)
     VALUES($1,$2,'tv','watching')
     ON CONFLICT(user_id, tmdb_id, media_type) DO NOTHING`,
    [req.user.sub, tv_tmdb_id]
  );
  res.json(rows[0]);
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`CineTrack backend on :${port}`));
