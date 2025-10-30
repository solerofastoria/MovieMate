package org.example.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "favorite_movies")
data class FavoriteMovie(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    val movieId: Long,
    val title: String,
    val genre: String? = null,
    val actor: String? = null,
    @ManyToOne @JoinColumn(name = "user_id") val user: User,
    val addedAt: LocalDateTime = LocalDateTime.now()
)