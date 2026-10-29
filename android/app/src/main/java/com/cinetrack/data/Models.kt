package com.cinetrack.data

import com.google.gson.annotations.SerializedName

data class TmdbItem(
  val id: Int = 0,
  @SerializedName("media_type") val mediaType: String? = null,
  val title: String? = null,
  val name: String? = null,
  val overview: String? = null,
  @SerializedName("poster_path") val posterPath: String? = null,
  @SerializedName("backdrop_path") val backdropPath: String? = null,
  @SerializedName("vote_average") val voteAverage: Double = 0.0,
  @SerializedName("release_date") val releaseDate: String? = null,
  @SerializedName("first_air_date") val firstAirDate: String? = null,
  val number_of_seasons: Int? = null,
  val runtime: Int? = null,
  val seasons: List<Season>? = null,
  val genres: List<Genre>? = null,
) {
  val displayTitle: String get() = title ?: name ?: "Senza titolo"
  val year: String? get() = (releaseDate ?: firstAirDate)?.take(4)
  val type: String get() = when (mediaType) { "tv" -> "tv"; "movie" -> "movie"; else -> if (seasons != null) "tv" else "movie" }
}

data class Season(val id: Int = 0, @SerializedName("season_number") val seasonNumber: Int = 0,
                  val name: String? = null, @SerializedName("episode_count") val episodeCount: Int = 0,
                  @SerializedName("poster_path") val posterPath: String? = null)

data class Genre(val id: Int = 0, val name: String = "")

data class TmdbSearchResponse(val page: Int = 1, val results: List<TmdbItem> = emptyList())

data class SeasonDetail(
  @SerializedName("season_number") val seasonNumber: Int = 0,
  val episodes: List<Episode> = emptyList(),
)

data class Episode(
  @SerializedName("episode_number") val episodeNumber: Int = 0,
  val name: String? = null,
  val overview: String? = null,
  @SerializedName("air_date") val airDate: String? = null,
  @SerializedName("still_path") val stillPath: String? = null,
  @SerializedName("runtime") val runtime: Int? = null,
)

data class ListItem(
  @SerializedName("tmdb_id") val tmdbId: Int,
  @SerializedName("media_type") val mediaType: String,
  val status: String,
  val rating: Int? = null,
  @SerializedName("updated_at") val updatedAt: String? = null,
)

data class ProgressEntry(
  @SerializedName("season_number") val seasonNumber: Int,
  @SerializedName("episode_number") val episodeNumber: Int,
  val watched: Boolean = true,
)

data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val user: UserDto? = null)
data class UserDto(val id: String, val email: String)

data class ListUpsert(
  @SerializedName("tmdb_id") val tmdbId: Int,
  @SerializedName("media_type") val mediaType: String,
  val status: String? = null,
  val rating: Int? = null,
)

data class ProgressUpsert(
  @SerializedName("tv_tmdb_id") val tvTmdbId: Int,
  @SerializedName("season_number") val seasonNumber: Int,
  @SerializedName("episode_number") val episodeNumber: Int,
  val watched: Boolean = true,
)

object Img {
  private const val BASE = "https://image.tmdb.org/t/p/"
  fun poster(path: String?) = if (path.isNullOrBlank()) null else BASE + "w342" + path
  fun backdrop(path: String?) = if (path.isNullOrBlank()) null else BASE + "w780" + path
}
