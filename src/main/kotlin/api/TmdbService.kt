package org.example.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.example.api.dto.TmdbMovie
import org.example.api.dto.TmdbResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

//todo: In Progress
@Service
class TmdbService(
    @Value("\${tmdb.api.token}") private val tmdbToken: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    fun searchMovies(query: String, page: Int = 1): List<TmdbMovie> {
        val url = "https://api.themoviedb.org/3/search/multi?" +
                "query=${query.replace(" ", "%20")}&include_adult=true&page=$page"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer $tmdbToken")
            .build()

        client.newCall(request).execute().use { response ->
            println(response.body)
            if (!response.isSuccessful) return emptyList()
            val body = response.body.string()
            val tmdbResponse: TmdbResponse = mapper.readValue(body, TmdbResponse::class.java)
            println(tmdbResponse)
            return tmdbResponse.results
        }
    }
}