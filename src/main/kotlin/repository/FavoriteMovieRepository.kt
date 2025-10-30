package org.example.repository

import org.example.entity.FavoriteMovie
import org.example.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FavoriteMovieRepository : JpaRepository<FavoriteMovie, Long> {
    fun findAllByUser(user: User): List<FavoriteMovie>
}