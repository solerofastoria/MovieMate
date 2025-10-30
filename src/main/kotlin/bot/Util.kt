package org.example.bot

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import org.example.entity.User

fun buildGenreKeyboard(user: User): InlineKeyboardMarkup {
    val genres = listOf(
        "komediya" to "🎭 Комедия",
        "uzhasy" to "😱 Ужасы",
        "drama" to "💔 Драма",
        "fantastika" to "🚀 Фантастика",
        "boevik" to "🔫 Боевик",
        "priklyucheniya" to "🏞️ Приключения",
        "semeynyy" to "👪 Семейный",
        "myuzikl" to "🎵 Мюзикл",
        "melodrama" to "💘 Мелодрама",
        "triller" to "🧠 Триллер",
        "detektiv" to "🕵️ Детектив",
        "detskiy" to "🧸 Детский",
        "fentezi" to "⚔️ Фэнтези",
        "biografiya" to "📖 Биография",
        "dokumentalnyy" to "🎓 Документальный",
        "sport" to "🎤 Спорт",
        "kriminal" to "🎯 Криминал",
        "voennyy" to "💣 Военный",
        "multfilm" to "🌍 Мультфильм",
        "anime" to "🧒 Аниме"
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

    rows.add(listOf(
        InlineKeyboardButton.CallbackData("✅ Готово", "genres_done")
    ))

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