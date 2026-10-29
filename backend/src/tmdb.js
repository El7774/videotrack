// Proxy minimo TMDB — la chiave resta sul server, mai nell'app.
const BASE = 'https://api.themoviedb.org/3';

function headers() {
  if (process.env.TMDB_BEARER) {
    return { Authorization: `Bearer ${process.env.TMDB_BEARER}` };
  }
  return {};
}

function withKey(url) {
  if (process.env.TMDB_BEARER) return url;
  const sep = url.includes('?') ? '&' : '?';
  return `${url}${sep}api_key=${process.env.TMDB_API_KEY}`;
}

export async function tmdbGet(path, params = {}) {
  const qs = new URLSearchParams({ language: 'it-IT', ...params }).toString();
  const url = withKey(`${BASE}${path}?${qs}`);
  const r = await fetch(url, { headers: headers() });
  if (!r.ok) throw new Error(`TMDB ${r.status}`);
  return r.json();
}
