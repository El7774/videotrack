package com.cinetrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
  private val api = provideApi()
  private val session = Session(app)

  private val _token = MutableStateFlow<String?>(null)
  val token: StateFlow<String?> = _token
  private val _list = MutableStateFlow<List<ListItem>>(emptyList())
  val myList: StateFlow<List<ListItem>> = _list
  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error

  init { viewModelScope.launch { session.restore(); _token.value = TokenHolder.token; if (_token.value != null) refreshList() } }

  fun auth(email: String, pass: String, register: Boolean, done: (Boolean) -> Unit) {
    viewModelScope.launch {
      try {
        val r = if (register) api.register(AuthRequest(email, pass)) else api.login(AuthRequest(email, pass))
        session.save(r.token); _token.value = r.token; refreshList(); done(true)
      } catch (e: Exception) { _error.value = "Login fallito: ${e.message}"; done(false) }
    }
  }
  fun logout() { viewModelScope.launch { session.clear(); _token.value = null; _list.value = emptyList() } }

  fun refreshList() { viewModelScope.launch { runCatching { _list.value = api.myList() } } }

  suspend fun trending() = runCatching { api.trending().results }.getOrElse { emptyList() }
  suspend fun search(q: String) = runCatching { api.search(q).results }.getOrElse { emptyList() }
  suspend fun detail(type: String, id: Int): TmdbItem? = runCatching {
    if (type == "tv") api.tv(id) else api.movie(id)
  }.getOrNull()
  suspend fun season(tvId: Int, n: Int) = runCatching { api.season(tvId, n) }.getOrNull()
  suspend fun progress(tvId: Int) = runCatching { api.progress(tvId) }.getOrElse { emptyList() }

  fun setStatus(item: TmdbItem, status: String) {
    viewModelScope.launch { runCatching { api.upsert(ListUpsert(item.id, item.type, status)); refreshList() } }
  }
  fun toggleEpisode(tvId: Int, s: Int, e: Int, watched: Boolean, onDone: () -> Unit = {}) {
    viewModelScope.launch { runCatching { api.setProgress(ProgressUpsert(tvId, s, e, watched)) }; onDone() }
  }
  fun statusOf(id: Int): String? = _list.value.firstOrNull { it.tmdbId == id }?.status
}
