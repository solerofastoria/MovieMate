package org.example.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KpMovie(
    val id: Int,
    val name: String?,
    val alternativeName: String? = null,
    val description: String?,
    val shortDescription: String?,
    val rating: Rating?,
    val poster: Poster?,
    val year: Int?,
    val genres: List<Genre>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rating(
    val kp: Double?,
    val imdb: Double?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Poster(
    val url: String?,
    val previewUrl: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Genre(
    val name: String
)