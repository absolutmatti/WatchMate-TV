package com.example.data.remote

import com.example.data.model.TMDbMovieDetails
import com.example.data.model.TMDbMultiSearchResponse
import com.example.data.model.TMDbTvDetails
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TMDbApiService {

    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "de-DE",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TMDbMultiSearchResponse

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "de-DE",
        @Query("append_to_response") appendToResponse: String = "videos,watch/providers"
    ): TMDbMovieDetails

    @GET("3/tv/{series_id}")
    suspend fun getTvDetails(
        @Path("series_id") seriesId: Long,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "de-DE",
        @Query("append_to_response") appendToResponse: String = "videos,watch/providers"
    ): TMDbTvDetails

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/"

        fun create(): TMDbApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(TMDbApiService::class.java)
        }
    }
}
