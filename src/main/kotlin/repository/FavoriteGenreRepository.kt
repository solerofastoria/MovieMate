package org.example.repository

import org.example.entity.FavoriteGenre
import org.example.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteGenreRepository : JpaRepository<FavoriteGenre, Long> {
    fun findByUserTelegramId(telegramId: Long): List<FavoriteGenre>
    fun deleteByUserAndGenreSlug(user: User, genreSlug: String)
}