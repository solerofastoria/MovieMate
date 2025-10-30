package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import jakarta.annotation.PostConstruct
import org.example.api.KpService
import org.example.api.TmdbService
import org.example.entity.FavoriteGenre
import org.example.entity.FavoriteMovie
import org.example.entity.User
import org.example.repository.FavoriteMovieRepository
import org.example.repository.RequestHistoryRepository
import org.example.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MovieMateBot(
    private val userRepository: UserRepository,
    private val favoriteMovieRepository: FavoriteMovieRepository,
    private val requestHistoryRepository: RequestHistoryRepository,
    private val tmdbService: TmdbService,
    private val kpService: KpService,
    @Value("\${telegram.bot.token}") private val botToken: String
) {


    private lateinit var bot: Bot

    private val waitingForMovieName = mutableSetOf<Long>()

    @PostConstruct
    fun start() {
        bot = bot {
            token = botToken
            logLevel = LogLevel.Error

            dispatch {

                command("start") {
                    val chatId = message.chat.id
                    val telegramUser = message.from!!

                    userRepository.findByTelegramId(telegramUser.id)
                        ?: userRepository.save(
                            User(
                                telegramId = telegramUser.id,
                                username = telegramUser.username,
                                firstName = telegramUser.firstName,
                                lastName = telegramUser.lastName
                            )
                        )

                    bot.sendMessage(
                        chatId = ChatId.fromId(chatId),
                        text = "\uD83C\uDF89 Добро пожаловать, ${telegramUser.firstName}!\n" +
                                "\n" +
                                "Я - ваш персональный киноконсультант! \uD83C\uDFAC\n" +
                                "\n" +
                                "Я ищу фильмы из множества источников чтобы найти именно то, что вам понравится!\n" +
                                "\n" +
                                "\uD83D\uDCCB Выбери действие:",
                        replyMarkup = buildMainMenu()
                    )
                }

                callbackQuery {
                    val data = callbackQuery.data
                    val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                    val telegramUser = callbackQuery.from
                    val user = userRepository.findByTelegramId(telegramUser.id) ?: return@callbackQuery

                    when {
                        data.startsWith("add_fav:") -> {
                            val movieTitle = data.removePrefix("add_fav:")
                            favoriteMovieRepository.save(
                                FavoriteMovie(
                                    movieId = 0,
                                    title = movieTitle,
                                    user = user
                                )
                            )
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Фильм '$movieTitle' добавлен в избранное!"
                            )
                        }

                        data.startsWith("more:") -> {
                            val mood = data.removePrefix("more:")
                            val movieTitle = "Ещё один фильм для '$mood'"
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Я подобрал вам: $movieTitle",
                                replyMarkup = buildInlineKeyboard(movieTitle, mood)
                            )
                        }

                        data.startsWith("toggle_genre:") -> {
                            val slug = data.removePrefix("toggle_genre:")
                            val genreName = when (slug) {
                                "комедия" -> "Комедия"
                                "ужасы" -> "Ужасы"
                                "драма" -> "Драма"
                                "фантастика" -> "Фантастика"
                                "боевик" -> "Боевик"
                                "приключения" -> "Приключения"
                                "семейный" -> "Семейный"
                                "мюзикл" -> "Мюзикл"
                                "мелодрама" -> "Мелодрама"
                                "триллер" -> "Триллер"
                                "детектив" -> "Детектив"
                                "детский" -> "Детский"
                                "фэнтези" -> "Фэнтези"
                                "биография" -> "Биография"
                                "документальный" -> "Документальный"
                                "спорт" -> "Спорт"
                                "криминал" -> "Криминал"
                                "военный" -> "Военный"
                                "мультфильм" -> "Мультфильм"
                                "аниме" -> "Аниме"
                                else -> slug
                            }
                            val existing = user.favoriteGenres.find { it.genreSlug == slug }
                            if (existing != null) {
                                user.favoriteGenres.remove(existing)
                            } else {
                                user.favoriteGenres.add(
                                    FavoriteGenre(
                                        genreName = genreName,
                                        genreSlug = slug,
                                        user = user
                                    )
                                )
                            }

                            userRepository.save(user)

                            bot.editMessageReplyMarkup(
                                chatId = ChatId.fromId(chatId),
                                messageId = callbackQuery.message?.messageId,
                                replyMarkup = buildGenreKeyboard(user)
                            )
                        }

                        data == "genres_done" -> {
                            bot.editMessageText(
                                chatId = ChatId.fromId(chatId),
                                messageId = callbackQuery.message?.messageId,
                                text = "✅ Предпочтения сохранены! Возвращаемся в главное меню.",
                                replyMarkup = null
                            )
                        }
                    }
                }

                text {
                    val chatId = message.chat.id
                    val text = message.text ?: return@text
                    val telegramUser = message.from!!

                    if (text.startsWith("/")) return@text

                    userRepository.findByTelegramId(telegramUser.id)
                        ?: userRepository.save(
                            User(
                                telegramId = telegramUser.id,
                                username = telegramUser.username,
                                firstName = telegramUser.firstName,
                                lastName = telegramUser.lastName
                            )
                        )
                    when {
                        text == "🔍 Поиск по названию" -> {
                            waitingForMovieName.add(chatId)
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Введите название фильма 🎬:"
                            )
                            return@text
                        }

                        chatId in waitingForMovieName -> {
                            waitingForMovieName.remove(chatId)

                            val query = text.trim()
                            val movies = kpService.getMoviesByName(query)

                            if (movies.isEmpty()) {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = "😢 Фильмы по запросу \"$query\" не найдены."
                                )
                                return@text
                            }

                            val moviesToShow = movies.take(3)
                            val messageText = moviesToShow.joinToString("\n\n") { movie ->
                                createTitle(movie)
                            }

                            val keyboard = createFavKeyboard(moviesToShow)

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = messageText,
                                parseMode = ParseMode.MARKDOWN,
                                replyMarkup = keyboard
                            )
                            return@text
                        }

                        text == "⚙️ Настройка предпочтений" -> {
                            val user = userRepository.findByTelegramId(telegramUser.id)
                                ?: userRepository.save(
                                    User(
                                        telegramId = telegramUser.id,
                                        username = telegramUser.username,
                                        firstName = telegramUser.firstName,
                                        lastName = telegramUser.lastName
                                    )
                                )

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "Выберите любимые жанры 🎬\n(нажимай повторно, чтобы удалить из списка)" +
                                        "\nПосле того как жанры будут выбраны, нажмите *Готово*",
                                replyMarkup = buildGenreKeyboard(user)
                            )
                        }

                        text == "🎨 Подборка фильмов по предпочтениям" -> {
                            val user = userRepository.findByTelegramId(telegramUser.id)
                                ?: userRepository.save(
                                    User(
                                        telegramId = telegramUser.id,
                                        username = telegramUser.username,
                                        firstName = telegramUser.firstName,
                                        lastName = telegramUser.lastName
                                    )
                                )
                            val favoriteGenres = user.favoriteGenres.map { it.genreSlug }

                            if (favoriteGenres.isEmpty()) {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = "⚙️ У вас пока не выбрано ни одного жанра.\n" +
                                            "Перейдите в *Настройку предпочтений*, чтобы выбрать любимые жанры.",
                                    parseMode = ParseMode.MARKDOWN
                                )
                                return@text
                            }
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "⏰ Одну минуту, собираем подборку по вашим предпочтениям"
                            )
                            val movies = kpService.getMoviesByGenres(favoriteGenres)
                            if (movies.isEmpty()) {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = "😢 Не удалось найти фильмы по вашим предпочтениям."
                                )
                                return@text
                            }

                            val moviesToShow = movies.take(5)
                            val messageText = moviesToShow.joinToString("\n\n") { movie ->
                                createTitle(movie)
                            }

                            val keyboard = createFavKeyboard(moviesToShow)

                            println("Отправляю сообщение:\n$messageText")
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "🎨 Подборка фильмов по вашим предпочтениям:\n\n$messageText",
                                parseMode = ParseMode.MARKDOWN,
                                replyMarkup = keyboard
                            )
                            return@text
                        }

                        text == "🎲 Случайный фильм" -> {
                            val movie = kpService.getRandomMovie()
                            if (movie != null) {
                                val posterUrl = movie.poster?.url ?: ""
                                val genresText = movie.genres?.joinToString(", ") { it.name } ?: "—"
                                val yearText = movie.year?.toString() ?: "—"
                                val title = listOf(movie.name, movie.alternativeName)
                                    .firstOrNull { !it.isNullOrBlank() } ?: "Без названия"

                                bot.sendPhoto(
                                    chatId = ChatId.fromId(chatId),
                                    photo = TelegramFile.ByUrl(posterUrl),
                                    caption = """
                                🎬 "$title" (${yearText})
                                ⭐ Кинопоиск: ${movie.rating?.kp ?: "—"}
                                ⭐ IMDb: ${movie.rating?.imdb ?: "—"}
                                🎭 Жанры: $genresText
                                     
                                ${movie.description ?: "Описание отсутствует"}
                                     """.trimIndent(),
                                    replyMarkup = InlineKeyboardMarkup.create(
                                        listOf(
                                            listOf(
                                                InlineKeyboardButton.CallbackData(
                                                    text = "Добавить в избранное",
                                                    callbackData = "add_fav:${movie.name}"
                                                ),
                                                InlineKeyboardButton.CallbackData(
                                                    text = "Случайный фильм",
                                                    callbackData = "random_movie"
                                                )
                                            )
                                        )
                                    )
                                )
                            } else {
                                bot.sendMessage(
                                    chatId = ChatId.fromId(chatId),
                                    text = "Не удалось получить случайный фильм. Попробуйте снова."
                                )
                            }
                            return@text
                        }

                        text == "👤 Профиль" -> {
                            val user = userRepository.findByTelegramId(telegramUser.id)
                                ?: userRepository.save(
                                    User(
                                        telegramId = telegramUser.id,
                                        username = telegramUser.username,
                                        firstName = telegramUser.firstName,
                                        lastName = telegramUser.lastName
                                    )
                                )

                            val name = buildString {
                                append(user.firstName ?: "")
                                if (!user.lastName.isNullOrBlank()) append(" ${user.lastName}")
                            }.ifBlank { "Без имени" }

                            val username = user.username?.let { "@$it" } ?: "—"

                            val genresText = if (user.favoriteGenres.isNotEmpty()) {
                                user.favoriteGenres.joinToString(", ") { it.genreName }
                            } else {
                                "Не выбраны 🎭"
                            }

                            val favoritesText = if (user.favorites.isNotEmpty()) {
                                user.favorites.joinToString("\n") { "• ${it.title}" }
                            } else {
                                "Пока пусто ❤️"
                            }

                            val profileText = """
                                👤 *Профиль пользователя*
                                
                                📛 Имя: *$name*
                                💬 Username: *$username*
                                
                                🎭 Любимые жанры:
                                $genresText
                                
                                ❤️ Избранные фильмы:
                                $favoritesText
                            """.trimIndent()

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = profileText,
                                parseMode = ParseMode.MARKDOWN,
                                replyMarkup = InlineKeyboardMarkup.create(
                                    listOf(
                                        listOf(
                                            InlineKeyboardButton.CallbackData(
                                                text = "⚙️ Настроить предпочтения",
                                                callbackData = "open_genres"
                                            ),
                                            InlineKeyboardButton.CallbackData(
                                                text = "🗑 Очистить избранное",
                                                callbackData = "clear_favorites"
                                            )
                                        )
                                    )
                                )
                            )
                            return@text
                        }
                        text == "❓ Помощь" -> {
                            val helpMessage = """
                            🎬 *MovieMate Bot* — подбор фильмов и сериалов
                              ✨ *Основные функции:*
                              • 🎭 Подборка фильмов по настроению или жанру
                              • ⚙️ Настройка личных предпочтений
                              • 🔍 Поиск фильмов по названию
                              • ❤️ Сохранение любимых фильмов в избранное
                              • 🎲 Случайный фильм на вечер
                              
                              🌐 *Используемые API:*
                             🎞️ В данный момент реализована интеграция с Kinopoisk API, 
                             также в работе находится интеграция с TMDb API
                            """.trimIndent()

                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = helpMessage,
                                parseMode = ParseMode.MARKDOWN_V2
                            )
                            return@text
                        }

                        else -> {
                            bot.sendMessage(
                                chatId = ChatId.fromId(chatId),
                                text = "\uD83C\uDFAC Я - ваш персональный киноконсультант! \uD83C\uDFAC\n" +
                                        "\n" +
                                        "Я ищу фильмы из множества источников чтобы найти именно то, что вам понравится!\n" +
                                        "\n" +
                                        "\uD83D\uDCCB Выбери действие из меню, чтобы продолжить:",
                                replyMarkup = buildMainMenu()
                            )
                        }
                    }
                }
            }
        }

        bot.startPolling()
    }
}