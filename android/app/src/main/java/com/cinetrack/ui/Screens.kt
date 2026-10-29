package com.cinetrack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinetrack.data.Img
import com.cinetrack.data.TmdbItem

@Composable
fun AuthScreen(vm: AppViewModel) {
  var email by remember { mutableStateOf("") }
  var pass by remember { mutableStateOf("") }
  var register by remember { mutableStateOf(false) }
  val err by vm.error.collectAsState()
  Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
    Text("🎬 CineTrack", style = MaterialTheme.typography.headlineLarge)
    Text("Film • Serie TV • Anime", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(pass, { pass = it }, label = { Text("Password (min 8)") }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Button(onClick = { vm.auth(email.trim(), pass, register) {} }, modifier = Modifier.fillMaxWidth()) {
      Text(if (register) "Registrati" else "Accedi")
    }
    TextButton(onClick = { register = !register }) {
      Text(if (register) "Hai già un account? Accedi" else "Nuovo qui? Registrati")
    }
    err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeShell(vm: AppViewModel, onOpen: (String, Int) -> Unit) {
  var tab by remember { mutableIntStateOf(0) }
  Scaffold(bottomBar = {
    NavigationBar {
      NavigationBarItem(tab == 0, { tab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
      NavigationBarItem(tab == 1, { tab = 1 }, icon = { Icon(Icons.Default.Search, null) }, label = { Text("Cerca") })
      NavigationBarItem(tab == 2, { tab = 2 }, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Libreria") })
    }
  }) { pad ->
    Box(Modifier.padding(pad)) {
      when (tab) {
        0 -> HomeTab(vm, onOpen)
        1 -> SearchTab(vm, onOpen)
        else -> LibraryTab(vm, onOpen)
      }
    }
  }
}

@Composable
fun HomeTab(vm: AppViewModel, onOpen: (String, Int) -> Unit) {
  var items by remember { mutableStateOf<List<TmdbItem>>(emptyList()) }
  LaunchedEffect(Unit) { items = vm.trending() }
  LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
    item { Text("Di tendenza questa settimana", style = MaterialTheme.typography.titleLarge) }
    item {
      LazyRow {
        items(items) { PosterCard(it) { onOpen(it.type, it.id) } }
      }
    }
  }
}

@Composable
fun SearchTab(vm: AppViewModel, onOpen: (String, Int) -> Unit) {
  var q by remember { mutableStateOf("") }
  var res by remember { mutableStateOf<List<TmdbItem>>(emptyList()) }
  Column(Modifier.fillMaxSize().padding(12.dp)) {
    OutlinedTextField(q, { q = it }, label = { Text("Cerca film, serie, anime…") }, modifier = Modifier.fillMaxWidth())
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Cerca") }
    // live search semplice
    LaunchedEffect(q) { if (q.length >= 2) res = vm.search(q) }
    LazyVerticalGrid(GridCells.Fixed(3), modifier = Modifier.fillMaxSize()) {
      items(res) { PosterCard(it) { onOpen(it.type, it.id) } }
    }
  }
}

@Composable
fun LibraryTab(vm: AppViewModel, onOpen: (String, Int) -> Unit) {
  val list by vm.myList.collectAsState()
  LaunchedEffect(Unit) { vm.refreshList() }
  var filter by remember { mutableStateOf("all") }
  Column(Modifier.fillMaxSize().padding(12.dp)) {
    Text("La mia libreria (${list.size})", style = MaterialTheme.typography.titleLarge)
    LazyRow { item {
      listOf("all" to "Tutti", "watchlist" to "Da vedere", "watching" to "In corso", "watched" to "Visti", "favorite" to "Preferiti").forEach { (k, l) ->
        FilterChip(filter == k, { filter = k }, { Text(l) }, modifier = Modifier.padding(end = 6.dp))
      }
    }}
    val shown = if (filter == "all") list else list.filter { it.status == filter }
    LazyColumn {
      items(shown) {
        ListItemRow("${it.mediaType} • ${it.status} • TMDB #${it.tmdbId}", Modifier.clickable { onOpen(it.mediaType, it.tmdbId) })
      }
    }
  }
}

@Composable
fun ListItemRow(t: String, m: Modifier = Modifier) {
  Card(m.fillMaxWidth().padding(vertical = 4.dp)) { Text(t, Modifier.padding(12.dp)) }
}

@Composable
fun PosterCard(it: TmdbItem, onClick: () -> Unit) {
  Card(Modifier.width(120.dp).padding(4.dp).clickable(onClick = onClick)) {
    Column {
      AsyncImage(Img.poster(it.posterPath), it.displayTitle, Modifier.height(170.dp).fillMaxWidth())
      Text(it.displayTitle, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(6.dp))
    }
  }
}

@Composable
fun DetailScreen(vm: AppViewModel, type: String, id: Int, onSeason: (Int) -> Unit, onBack: () -> Unit) {
  var d by remember { mutableStateOf<TmdbItem?>(null) }
  LaunchedEffect(id) { d = vm.detail(type, id) }
  LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
    item {
      Row { TextButton(onClick = onBack) { Text("← Indietro") } }
      Text(d?.displayTitle ?: "…", style = MaterialTheme.typography.headlineSmall)
      Text("${d?.year ?: ""} • ⭐ ${d?.voteAverage ?: 0}", style = MaterialTheme.typography.bodyMedium)
      Spacer(Modifier.height(8.dp))
      d?.overview?.let { Text(it) }
      Spacer(Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { d?.let { vm.setStatus(it, "watchlist") } }) { Text("Da vedere") }
        Button(onClick = { d?.let { vm.setStatus(it, "watched") } }) { Text("Visto ✓") }
        Button(onClick = { d?.let { vm.setStatus(it, "favorite") } }) { Text("♥") }
      }
      if (type == "tv") {
        Spacer(Modifier.height(12.dp))
        Text("Stagioni: ${d?.number_of_seasons ?: "?"}", style = MaterialTheme.typography.titleMedium)
        d?.seasons?.filter { it.seasonNumber > 0 }?.forEach { s ->
          Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSeason(s.seasonNumber) }) {
            Text("S${s.seasonNumber} • ${s.name ?: ""} (${s.episodeCount} ep)", Modifier.padding(12.dp))
          }
        }
      }
    }
  }
}

@Composable
fun SeasonScreen(vm: AppViewModel, tvId: Int, season: Int, onBack: () -> Unit) {
  var eps by remember { mutableStateOf<com.cinetrack.data.SeasonDetail?>(null) }
  var prog by remember { mutableStateOf<List<com.cinetrack.data.ProgressEntry>>(emptyList()) }
  var refresh by remember { mutableIntStateOf(0) }
  LaunchedEffect(season, refresh) { eps = vm.season(tvId, season); prog = vm.progress(tvId) }
  val done = prog.filter { it.seasonNumber == season && it.watched }.map { it.episodeNumber }.toSet()
  LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
    item {
      TextButton(onClick = onBack) { Text("← Indietro") }
      Text("Stagione $season — ${done.size}/${eps?.episodes?.size ?: 0} visti",
        style = MaterialTheme.typography.titleLarge)
      val total = eps?.episodes?.size ?: 0
      if (total > 0) LinearProgressIndicator(done.size / total.toFloat(), Modifier.fillMaxWidth().padding(vertical = 8.dp))
    }
    eps?.episodes?.let { list ->
      items(list) { ep ->
        val seen = ep.episodeNumber in done
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
          Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(seen, { c -> vm.toggleEpisode(tvId, season, ep.episodeNumber, c) { refresh++ } })
            Column(Modifier.weight(1f)) {
              Text("E${ep.episodeNumber} • ${ep.name ?: ""}", style = MaterialTheme.typography.titleSmall)
              ep.overview?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall) }
            }
          }
        }
      }
    }
  }
}
