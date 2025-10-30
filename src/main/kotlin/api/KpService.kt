package org.example.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.example.api.dto.KpMovie
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class KpService(
    @Value("\${kinopoisk.api.token}") private val kpToken: String
) {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    fun getRandomMovie(): KpMovie? {
        val request = Request.Builder()
            .url("https://api.kinopoisk.dev/v1.4/movie/random?year=1980-2025&rating.kp=7-10&rating.imdb=7-10")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", kpToken)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body.string()
            return mapper.readValue(body, KpMovie::class.java)
        }
    }

    fun getMoviesByName(name: String): List<KpMovie?> {
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
        val request = Request.Builder()
            .url("https://api.kinopoisk.dev/v1.4/movie/search?page=1&limit=3&query=${encodedName}")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("X-API-KEY", kpToken)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()

            val body = response.body.string()

            val jsonNode = mapper.readTree(body)
            val docsNode = jsonNode["docs"] ?: return emptyList()

            return docsNode.map { doc ->
                mapper.treeToValue(doc, KpMovie::class.java)
            }
        }
    }

    fun getMoviesByGenres(genres: List<String>): List<KpMovie?> {
        if (genres.isEmpty()) return emptyList()

        val result = mutableListOf<KpMovie>()

        repeat(5) {
            val genresParams = genres.joinToString("&") {
                "genres.name=" + URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
            }
            val url = "https://api.kinopoisk.dev/v1.4/movie/random?" +
                    "year=1980-2025&" +
                    "rating.kp=6-10&" +
                    genresParams
            val request = Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("X-API-KEY", kpToken)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body.string()
                    if (body.isNotBlank()) {
                        val movie = mapper.readValue(body, KpMovie::class.java)
                        result.add(movie)
                    } else {
                        println("Пустое тело ответа")
                    }
                } else {
                    println("Ошибка запроса [${response.code}]: ${response.message}")
                }
            }
            Thread.sleep(200)
        }

        return result.distinctBy { it.id }
    }
}