package org.example.service

import org.example.entity.FavoriteMovie
import org.example.entity.RequestHistory
import org.example.entity.User
import org.example.repository.FavoriteMovieRepository
import org.example.repository.RequestHistoryRepository
import org.springframework.stereotype.Component

@Component
class MovieService(
    private val favoriteRepo: FavoriteMovieRepository,
    private val historyRepo: RequestHistoryRepository
) {

    fun addFakeFavorite(user: User, title: String) {
        val movie = FavoriteMovie(
            movieId = (1000..9999).random().toLong(),
            title = title,
            user = user
        )
        favoriteRepo.save(movie)
    }

    fun getFavorites(user: User): List<FavoriteMovie> =
        favoriteRepo.findAllByUser(user)

    fun saveRequest(user: User, query: String, fakeMovieId: Long) {
        historyRepo.save(RequestHistory(moodOrGenre = query, responseMovieId = fakeMovieId, user = user))
    }
}