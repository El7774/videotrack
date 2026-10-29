package com.cinetrack.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ds by preferencesDataStore("cinetrack")
private val KEY_TOKEN = stringPreferencesKey("jwt")

class Session(private val ctx: Context) {
  suspend fun token(): String? = ctx.ds.data.map { it[KEY_TOKEN] }.first()
  suspend fun save(token: String) {
    ctx.ds.edit { it[KEY_TOKEN] = token }
    TokenHolder.token = token
  }
  suspend fun clear() {
    ctx.ds.edit { it.remove(KEY_TOKEN) }
    TokenHolder.token = null
  }
  suspend fun restore() { TokenHolder.token = token() }
}
