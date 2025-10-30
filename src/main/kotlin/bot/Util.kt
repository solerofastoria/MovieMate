package org.example.bot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import org.example.api.dto.KpMovie
import org.example.entity.User

fun buildGenreKeyboard(user: User): InlineKeyboardMarkup {
    val genres = listOf(
        "комедия" to "🎭 Комедия",
        "ужасы" to "😱 Ужасы",
        "драма" to "💔 Драма",
        "фантастика" to "🚀 Фантастика",
        "боевик" to "🔫 Боевик",
        "приключения" to "🏞️ Приключения",
        "семейный" to "👪 Семейный",
        "мюзикл" to "🎵 Мюзикл",
        "мелодрама" to "💘 Мелодрама",
        "триллер" to "🧠 Триллер",
        "детектив" to "🕵️ Детектив",
        "детский" to "🧸 Детский",
        "фэнтези" to "⚔️ Фэнтези",
        "биография" to "📖 Биография",
        "документальный" to "🎓 Документальный",
        "спорт" to "🎤 Спорт",
        "криминал" to "🎯 Криминал",
        "военный" to "💣 Военный",
        "мультфильм" to "🌍 Мультфильм",
        "аниме " to "🧒 Аниме"
    )

    val rows = genres.chunked(3).map { chunk ->
        chunk.map { (slug, emojiName) ->
            val selected = user.favoriteGenres.any { it.genreSlug == slug }
            InlineKeyboardButton.CallbackData(
                text = if (selected) "✅ $emojiName" else emojiName,
                callbackData = "toggle_genre:$slug"
            )
        }
    }.toMutableList()

    rows.add(
        listOf(
            InlineKeyboardButton.CallbackData("✅ Готово", "genres_done")
        )
    )

    return InlineKeyboardMarkup.create(*rows.toTypedArray())
}

fun buildMainMenu(): KeyboardReplyMarkup {
    return KeyboardReplyMarkup(
        keyboard = listOf(
            listOf(
                KeyboardButton("🎬 Подборка фильмов по настроению"),
                KeyboardButton("🎨 Подборка фильмов по предпочтениям")
            ),
            listOf(
                KeyboardButton("⚙️ Настройка предпочтений"),
                KeyboardButton("👤 Профиль")
            ),
            listOf(
                KeyboardButton("🔍 Поиск по названию"),
                KeyboardButton("❓ Помощь")
            ),
            listOf(
                KeyboardButton("🎲 Случайный фильм")
            )
        ),
        resizeKeyboard = true,
        oneTimeKeyboard = false
    )
}

fun buildInlineKeyboard(movieTitle: String, mood: String): InlineKeyboardMarkup {
    return InlineKeyboardMarkup.create(
        listOf(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = "Добавить в избранное",
                    callbackData = "add_fav:$movieTitle"
                ),
                InlineKeyboardButton.CallbackData(
                    text = "Смотреть ещё",
                    callbackData = "more:$mood"
                )
            )
        )
    )
}

fun createTitle(movie: KpMovie?): String {
    val genres = movie?.genres?.joinToString(", ") { it.name } ?: "—"
    val description = listOf(movie?.description, movie?.shortDescription)
        .firstOrNull { !it.isNullOrBlank() } ?: "Описание отсутствует."
    val year = movie?.year?.toString() ?: "—"
    val name = listOf(movie?.name, movie?.alternativeName)
        .firstOrNull { !it.isNullOrBlank() } ?: "Без названия"
    return "🎬 *$name* ($year)\n🎭 Жанры: $genres\n⭐ Кинопоиск: ${movie?.rating?.kp ?: "—"}\n$description"
}

fun createFavKeyboard(movies: List<KpMovie?>): InlineKeyboardMarkup {
    val buttons = movies.map { movie ->
        val title = listOf(movie?.name, movie?.alternativeName)
            .firstOrNull { !it.isNullOrBlank() } ?: "Без названия"
        InlineKeyboardButton.CallbackData(
            text = "❤️ $title",
            callbackData = "add_fav:$title"
        )
    }.chunked(2)

    return InlineKeyboardMarkup.create(
        buttons.map { row -> row.toList() }
    )
}