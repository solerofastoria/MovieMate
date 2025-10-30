package org.example.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovie(
    val id: Long,
    val title: String,
    val overview: String?,
    val release_date: String?,
    val poster_path: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbResponse(
    val page: Int,
    val results: List<TmdbMovie>
)