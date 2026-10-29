package com.cinetrack.data

import com.cinetrack.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface CineApi {
  @POST("auth/register") suspend fun register(@Body b: AuthRequest): AuthResponse
  @POST("auth/login") suspend fun login(@Body b: AuthRequest): AuthResponse

  @GET("trending") suspend fun trending(): TmdbSearchResponse
  @GET("search") suspend fun search(@Query("q") q: String): TmdbSearchResponse
  @GET("movie/{id}") suspend fun movie(@Path("id") id: Int): TmdbItem
  @GET("tv/{id}") suspend fun tv(@Path("id") id: Int): TmdbItem
  @GET("tv/{id}/season/{n}") suspend fun season(@Path("id") id: Int, @Path("n") n: Int): SeasonDetail

  @GET("list") suspend fun myList(): List<ListItem>
  @PUT("list") suspend fun upsert(@Body b: ListUpsert): ListItem
  @DELETE("list/{media}/{id}") suspend fun remove(@Path("media") media: String, @Path("id") id: Int): Map<String, Boolean>

  @GET("progress/{tvId}") suspend fun progress(@Path("tvId") tvId: Int): List<ProgressEntry>
  @PUT("progress") suspend fun setProgress(@Body b: ProgressUpsert): ProgressEntry
}

object TokenHolder { @Volatile var token: String? = null }

fun provideApi(): CineApi {
  val auth = Interceptor { chain ->
    val req = chain.request().newBuilder().apply {
      TokenHolder.token?.let { addHeader("Authorization", "Bearer $it") }
    }.build()
    chain.proceed(req)
  }
  val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
  val client = OkHttpClient.Builder().addInterceptor(auth).addInterceptor(log).build()
  return Retrofit.Builder()
    .baseUrl(BuildConfig.API_BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build().create(CineApi::class.java)
}
