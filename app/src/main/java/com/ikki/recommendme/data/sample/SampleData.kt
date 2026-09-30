package com.ikki.recommendme.data.sample

import com.ikki.recommendme.domain.model.CastMember
import com.ikki.recommendme.domain.model.MediaDetail
import com.ikki.recommendme.domain.model.MediaItem
import com.ikki.recommendme.domain.model.MediaType
import com.ikki.recommendme.domain.model.WatchStatus

/**
 * Development data used while the TMDB API key is not configured yet.
 * [com.ikki.recommendme.data.repository.DefaultMediaRepository] serves this
 * data automatically until API_KEY is set in TmdbConfig.
 */
object SampleData {

    private fun movie(
        id: Int, title: String, overview: String, genres: List<String>,
        vote: Double, date: String, popularity: Double, runtime: Int
    ): Pair<MediaItem, Int> = MediaItem(
        id = id, mediaType = MediaType.MOVIE, title = title, overview = overview,
        posterPath = null, backdropPath = null, voteAverage = vote, voteCount = 1000,
        releaseDate = date, genres = genres, popularity = popularity
    ) to runtime

    private fun tv(
        id: Int, name: String, overview: String, genres: List<String>,
        vote: Double, date: String, popularity: Double, seasons: Int, episodes: Int
    ): Triple<MediaItem, Int, Int> = Triple(
        MediaItem(
            id = id, mediaType = MediaType.TV, title = name, overview = overview,
            posterPath = null, backdropPath = null, voteAverage = vote, voteCount = 1000,
            releaseDate = date, genres = genres, popularity = popularity
        ), seasons, episodes
    )

    private val movieData = listOf(
        movie(1001, "Inception",
            "A skilled thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
            listOf("Action", "Science Fiction", "Thriller"), 8.4, "2010-07-16", 120.0, 148),
        movie(1002, "The Dark Knight",
            "Batman raises the stakes in his war on crime as the Joker plunges Gotham into anarchy.",
            listOf("Drama", "Action", "Crime"), 8.5, "2008-07-18", 115.0, 152),
        movie(1003, "Interstellar",
            "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
            listOf("Adventure", "Drama", "Science Fiction"), 8.4, "2014-11-05", 110.0, 169),
        movie(1004, "Oppenheimer",
            "The story of J. Robert Oppenheimer and the creation of the atomic bomb during World War II.",
            listOf("Drama", "History", "Thriller"), 8.1, "2023-07-19", 95.0, 180),
        movie(1005, "Dune: Part Two",
            "Paul Atreides unites with the Fremen to wage war against House Harkonnen.",
            listOf("Science Fiction", "Adventure", "Drama"), 8.2, "2024-02-27", 130.0, 166),
        movie(1006, "Barbie",
            "Barbie suffers a crisis that leads her to question her world and her existence.",
            listOf("Comedy", "Adventure", "Fantasy"), 7.1, "2023-07-19", 100.0, 114),
        movie(1007, "Spider-Man: Across the Spider-Verse",
            "Miles Morales is catapulted across the Multiverse and meets a team of Spider-People.",
            listOf("Animation", "Action", "Adventure"), 8.3, "2023-05-31", 105.0, 140),
        movie(1008, "The Batman",
            "Batman ventures into Gotham's underworld when a sadistic killer leaves behind a trail of cryptic clues.",
            listOf("Crime", "Mystery", "Thriller"), 7.7, "2022-03-01", 98.0, 176),
        movie(1009, "Parasite",
            "Greed and class discrimination threaten the newly formed symbiotic relationship between two families.",
            listOf("Thriller", "Drama", "Comedy"), 8.5, "2019-05-30", 90.0, 132),
        movie(1010, "Avengers: Endgame",
            "The Avengers assemble once more to reverse Thanos' actions and restore balance to the universe.",
            listOf("Action", "Adventure", "Science Fiction"), 8.3, "2019-04-24", 140.0, 181),
        movie(1011, "John Wick: Chapter 4",
            "John Wick uncovers a path to defeating the High Table, but must face a new enemy.",
            listOf("Action", "Thriller", "Crime"), 7.7, "2023-03-22", 92.0, 169),
        movie(1012, "Everything Everywhere All at Once",
            "An aging Chinese immigrant is swept into an adventure where she alone can save the multiverse.",
            listOf("Science Fiction", "Adventure", "Comedy"), 7.8, "2022-03-24", 85.0, 139),
        movie(1013, "Get Out",
            "A young African-American visits his white girlfriend's parents for the weekend, where his simmering unease boils into horror.",
            listOf("Horror", "Mystery", "Thriller"), 7.6, "2017-02-24", 78.0, 104),
        movie(1014, "The Godfather",
            "The aging patriarch of an organized crime dynasty transfers control to his reluctant son.",
            listOf("Crime", "Drama"), 8.7, "1972-03-24", 88.0, 175),
        movie(1015, "Titanic",
            "A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the ill-fated R.M.S. Titanic.",
            listOf("Romance", "Drama"), 7.9, "1997-12-19", 86.0, 194),
        movie(1016, "Inside Out 2",
            "Riley enters her teenage years as Headquarters undergoes a sudden demolition to make room for new emotions.",
            listOf("Animation", "Family", "Comedy"), 7.6, "2024-06-11", 112.0, 96),
        movie(1017, "Dune",
            "Paul Atreides, a brilliant young man born into a great destiny, must travel to the most dangerous planet in the universe.",
            listOf("Science Fiction", "Adventure", "Drama"), 7.8, "2021-10-21", 102.0, 155),
        movie(1018, "The Shawshank Redemption",
            "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
            listOf("Drama", "Crime"), 8.7, "1994-09-23", 84.0, 142)
    )

    private val tvData = listOf(
        tv(2001, "Breaking Bad",
            "A chemistry teacher diagnosed with cancer teams up with a former student to secure his family's future by making and selling meth.",
            listOf("Crime", "Drama", "Thriller"), 8.9, "2008-01-20", 118.0, 5, 62),
        tv(2002, "Stranger Things",
            "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
            listOf("Drama", "Fantasy", "Horror"), 8.6, "2016-07-15", 125.0, 4, 34),
        tv(2003, "Game of Thrones",
            "Nine noble families fight for control over the lands of Westeros, while an ancient enemy returns after millennia.",
            listOf("Drama", "Fantasy", "Adventure"), 8.4, "2011-04-17", 115.0, 8, 73),
        tv(2004, "The Office",
            "A mockumentary on a group of typical office workers, where daily work routines blur the lives of the employees.",
            listOf("Comedy"), 8.6, "2005-03-24", 95.0, 9, 201),
        tv(2005, "Dark",
            "A missing child sets four families on a frantic hunt for answers as they unearth a mind-bending mystery that spans three generations.",
            listOf("Mystery", "Drama", "Science Fiction"), 8.4, "2017-12-01", 82.0, 3, 26),
        tv(2006, "The Last of Us",
            "Twenty years after modern civilization has been destroyed, a hardened survivor is hired to smuggle a teenage girl across a dystopian America.",
            listOf("Drama", "Action", "Adventure"), 8.5, "2023-01-15", 108.0, 2, 16),
        tv(2007, "Wednesday",
            "Smart, sarcastic and a little dead inside, Wednesday Addams investigates a murder spree while making new friends at Nevermore Academy.",
            listOf("Comedy", "Mystery", "Fantasy"), 8.1, "2022-11-23", 104.0, 1, 8),
        tv(2008, "Squid Game",
            "Hundreds of cash-strapped players accept an invitation to compete in children's games for a tempting prize, with deadly stakes.",
            listOf("Thriller", "Drama", "Mystery"), 7.8, "2021-09-17", 110.0, 2, 16),
        tv(2009, "The Witcher",
            "Geralt of Rivia, a mutated monster-hunter for hire, journeys toward his destiny in a turbulent world where people often prove more wicked than beasts.",
            listOf("Fantasy", "Action", "Drama"), 7.1, "2019-12-20", 96.0, 3, 24),
        tv(2010, "Arcane",
            "Amid the stark discord of twin cities Piltover and Zaun, two sisters fight on rival sides of a war between magic technologies and clashing convictions.",
            listOf("Animation", "Action", "Fantasy"), 9.0, "2021-11-06", 113.0, 2, 18)
    )

    private val castMap: Map<String, List<Pair<String, String>>> = mapOf(
        "movie-1001" to listOf("Leonardo DiCaprio" to "Cobb", "Joseph Gordon-Levitt" to "Arthur", "Elliot Page" to "Ariadne", "Tom Hardy" to "Eames"),
        "movie-1002" to listOf("Christian Bale" to "Bruce Wayne", "Heath Ledger" to "The Joker", "Aaron Eckhart" to "Harvey Dent", "Gary Oldman" to "Jim Gordon"),
        "movie-1003" to listOf("Matthew McConaughey" to "Cooper", "Anne Hathaway" to "Brand", "Jessica Chastain" to "Murph", "Michael Caine" to "Professor Brand"),
        "movie-1004" to listOf("Cillian Murphy" to "J. Robert Oppenheimer", "Emily Blunt" to "Kitty Oppenheimer", "Robert Downey Jr." to "Lewis Strauss", "Matt Damon" to "Leslie Groves"),
        "movie-1005" to listOf("Timothée Chalamet" to "Paul Atreides", "Zendaya" to "Chani", "Rebecca Ferguson" to "Lady Jessica", "Javier Bardem" to "Stilgar"),
        "movie-1006" to listOf("Margot Robbie" to "Barbie", "Ryan Gosling" to "Ken", "America Ferrera" to "Gloria", "Kate McKinnon" to "Weird Barbie"),
        "movie-1007" to listOf("Shameik Moore" to "Miles Morales", "Hailee Steinfeld" to "Gwen Stacy", "Oscar Isaac" to "Miguel O'Hara", "Jake Johnson" to "Peter B. Parker"),
        "movie-1008" to listOf("Robert Pattinson" to "Bruce Wayne", "Zoë Kravitz" to "Selina Kyle", "Paul Dano" to "The Riddler", "Jeffrey Wright" to "James Gordon"),
        "movie-1009" to listOf("Song Kang-ho" to "Kim Ki-taek", "Choi Woo-shik" to "Kim Ki-woo", "Park So-dam" to "Ki-jung", "Lee Sun-kyun" to "Park Dong-ik"),
        "movie-1010" to listOf("Robert Downey Jr." to "Tony Stark", "Chris Evans" to "Steve Rogers", "Scarlett Johansson" to "Natasha Romanoff", "Josh Brolin" to "Thanos"),
        "movie-1011" to listOf("Keanu Reeves" to "John Wick", "Donnie Yen" to "Caine", "Bill Skarsgård" to "Marquis", "Laurence Fishburne" to "Bowery King"),
        "movie-1012" to listOf("Michelle Yeoh" to "Evelyn Wang", "Ke Huy Quan" to "Waymond Wang", "Stephanie Hsu" to "Joy / Jobu Tupaki", "Jamie Lee Curtis" to "Deirdre"),
        "movie-1013" to listOf("Daniel Kaluuya" to "Chris Washington", "Allison Williams" to "Rose Armitage", "Bradley Whitford" to "Dean Armitage", "Caleb Landry Jones" to "Graham"),
        "movie-1014" to listOf("Marlon Brando" to "Don Vito Corleone", "Al Pacino" to "Michael Corleone", "James Caan" to "Sonny Corleone", "Robert Duvall" to "Tom Hagen"),
        "movie-1015" to listOf("Leonardo DiCaprio" to "Jack Dawson", "Kate Winslet" to "Rose DeWitt Bukater", "Billy Zane" to "Caledon Hockley", "Gloria Stuart" to "Old Rose"),
        "movie-1016" to listOf("Amy Poehler" to "Joy", "Maya Hawke" to "Anxiety", "Phyllis Smith" to "Sadness", "Tony Hale" to "Fear"),
        "movie-1017" to listOf("Timothée Chalamet" to "Paul Atreides", "Zendaya" to "Chani", "Oscar Isaac" to "Leto Atreides", "Rebecca Ferguson" to "Lady Jessica"),
        "movie-1018" to listOf("Tim Robbins" to "Andy Dufresne", "Morgan Freeman" to "Ellis Boyd Redding", "Bob Gunton" to "Warden Norton", "William Sadler" to "Heywood"),
        "tv-2001" to listOf("Bryan Cranston" to "Walter White", "Aaron Paul" to "Jesse Pinkman", "Anna Gunn" to "Skyler White", "Dean Norris" to "Hank Schrader"),
        "tv-2002" to listOf("Millie Bobby Brown" to "Eleven", "Finn Wolfhard" to "Mike Wheeler", "Winona Ryder" to "Joyce Byers", "David Harbour" to "Jim Hopper"),
        "tv-2003" to listOf("Emilia Clarke" to "Daenerys Targaryen", "Kit Harington" to "Jon Snow", "Peter Dinklage" to "Tyrion Lannister", "Lena Headey" to "Cersei Lannister"),
        "tv-2004" to listOf("Steve Carell" to "Michael Scott", "John Krasinski" to "Jim Halpert", "Rainn Wilson" to "Dwight Schrute" , "Jenna Fischer" to "Pam Beesly"),
        "tv-2005" to listOf("Louis Hofmann" to "Jonas Kahnwald", "Lisa Vicari" to "Martha Nielsen", "Karoline Eichhorn" to "Charlotte Doppler", "Maja Schöne" to "Hannah Kahnwald"),
        "tv-2006" to listOf("Pedro Pascal" to "Joel Miller", "Bella Ramsey" to "Ellie Williams", "Gabriel Luna" to "Tommy Miller", "Anna Torv" to "Tess"),
        "tv-2007" to listOf("Jenna Ortega" to "Wednesday Addams", "Gwendoline Christie" to "Principal Larissa Weems", "Emma Myers" to "Enid Sinclair", "Hunter Doohan" to "Tyler Galpin"),
        "tv-2008" to listOf("Lee Jung-jae" to "Seong Gi-hun", "Park Hae-soo" to "Cho Sang-woo", "Jung Ho-yeon" to "Kang Sae-byeok", "Wi Ha-jun" to "Hwang Jun-ho"),
        "tv-2009" to listOf("Henry Cavill" to "Geralt of Rivia", "Anya Chalotra" to "Yennefer", "Freya Allan" to "Ciri", "Joey Batey" to "Jaskier"),
        "tv-2010" to listOf("Hailee Steinfeld" to "Vi", "Ella Purnell" to "Jinx", "Kevin Alejandro" to "Jayce", "Katie Leung" to "Caitlyn")
    )

    private val taglines: Map<String, String> = mapOf(
        "movie-1001" to "Your mind is the scene of the crime.",
        "movie-1002" to "Why so serious?",
        "movie-1003" to "Mankind was born on Earth. It was never meant to die here.",
        "movie-1014" to "An offer you can't refuse.",
        "tv-2001" to "Change is not a comfortable process.",
        "tv-2003" to "When you play the game of thrones, you win or you die."
    )

    private val movieRuntimes: Map<String, Int> = movieData.associate { (m, runtime) -> m.key to runtime }
    private val tvMeta: Map<String, Pair<Int, Int>> = tvData.associate { (t, seasons, episodes) -> t.key to (seasons to episodes) }

    val allItems: List<MediaItem> = movieData.map { it.first } + tvData.map { it.first }
    val movies: List<MediaItem> = movieData.map { it.first }
    val tvShows: List<MediaItem> = tvData.map { it.first }

    fun trending(): List<MediaItem> = listOf(
        "tv-2010", "movie-1005", "tv-2006", "movie-1016", "tv-2007",
        "movie-1004", "tv-2008", "movie-1007", "tv-2002", "movie-1011"
    ).let { keys -> keys.mapNotNull { k -> allItems.find { it.key == k } } }

    fun topRated(): List<MediaItem> = allItems.sortedByDescending { it.voteAverage }

    fun newReleases(): List<MediaItem> =
        allItems.sortedByDescending { it.year ?: 0 }.take(12)

    fun search(query: String): List<MediaItem> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return allItems.filter { it.title.contains(q, ignoreCase = true) }
    }

    fun detailFor(item: MediaItem): MediaDetail {
        val cast = castMap[item.key].orEmpty().mapIndexed { i, (name, character) ->
            CastMember(id = item.id * 10 + i, name = name, character = character, profilePath = null)
        }
        val idx = allItems.indexOf(item)
        val similar = (1..8).map { allItems[(idx + it) % allItems.size] }
        return MediaDetail(
            item = item,
            tagline = taglines[item.key],
            status = if (item.mediaType == MediaType.MOVIE) "Released" else "Returning Series",
            originalLanguage = "en",
            runtimeMinutes = movieRuntimes[item.key],
            numberOfSeasons = tvMeta[item.key]?.first,
            numberOfEpisodes = tvMeta[item.key]?.second,
            cast = cast,
            videos = emptyList(), // trailers arrive with the TMDB API key
            similar = similar
        )
    }

    /** Watchlist demo entries are created on demand by the repository if needed. */
    val demoStatus = WatchStatus.WATCHING
}
