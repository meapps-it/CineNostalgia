package com.meapps.cinenostalgia.data

import com.meapps.cinenostalgia.network.WikidataApi
import com.meapps.cinenostalgia.network.WikipediaApi
import org.jsoup.Jsoup

data class KnowledgeEnrichment(
    val extendedOverview: String? = null,
    val spoiler: String? = null,
    val curiosities: List<String> = emptyList(),
    val locations: List<FilmLocation> = emptyList(),
    val sources: List<String> = emptyList()
)

class KnowledgeRepository(
    private val wikipedia: WikipediaApi,
    private val wikidata: WikidataApi
) {
    suspend fun personBiography(name: String): Pair<String?, String?> {
        val page = wikipedia.search(query = name).query?.search?.firstOrNull()?.title ?: return null to null
        val biography = clean(wikipedia.sectionText(page = page, section = "0").parse?.text)
        return biography to "Wikipedia · $page"
    }

    suspend fun enrich(movie: MovieSummary): KnowledgeEnrichment {
        val wikipediaData = runCatching { wikipediaData(movie) }.getOrDefault(KnowledgeEnrichment())
        val locations = runCatching { wikidataLocations(movie.id, movie.mediaType) }.getOrDefault(emptyList())
        return wikipediaData.copy(
            locations = locations,
            sources = buildList {
                addAll(wikipediaData.sources)
                if (locations.isNotEmpty()) add("Wikidata")
            }.distinct()
        )
    }

    private suspend fun wikipediaData(movie: MovieSummary): KnowledgeEnrichment {
        val kind = if (movie.mediaType == "tv") "serie televisiva" else "film"
        val searchTerm = "${movie.title} ${movie.year} $kind"
        val page = wikipedia.search(query = searchTerm).query?.search?.firstOrNull()?.title ?: return KnowledgeEnrichment()
        val sections = wikipedia.sections(page = page).parse?.sections.orEmpty()
        val intro = clean(wikipedia.sectionText(page = page, section = "0").parse?.text)
        val plotIndex = sections.firstOrNull { it.line.equals("Trama", true) || it.line.equals("Sinossi", true) }?.index
        val productionIndex = sections.firstOrNull {
            it.line.equals("Produzione", true) || it.line.equals("Realizzazione", true) || it.line.equals("Riprese", true)
        }?.index
        val triviaIndex = sections.firstOrNull { it.line.contains("Curios", true) || it.line.contains("Retroscena", true) }?.index
        val plot = plotIndex?.let { clean(wikipedia.sectionText(page = page, section = it).parse?.text) }
        val production = (triviaIndex ?: productionIndex)?.let { clean(wikipedia.sectionText(page = page, section = it).parse?.text) }
        return KnowledgeEnrichment(
            extendedOverview = intro?.takeIf { it.length >= 180 },
            spoiler = plot?.takeIf { it.length >= 120 },
            curiosities = production?.takeIf { it.length >= 80 }?.let(::listOf).orEmpty(),
            sources = listOf("Wikipedia · $page")
        )
    }

    private suspend fun wikidataLocations(tmdbId: Int, mediaType: String): List<FilmLocation> {
        val tmdbProperty = if (mediaType == "tv") "P4983" else "P4947"
        val query = """
            SELECT DISTINCT ?location ?locationLabel ?coord ?description WHERE {
              ?film wdt:$tmdbProperty "$tmdbId"; wdt:P915 ?location.
              OPTIONAL { ?location wdt:P625 ?coord. }
              OPTIONAL { ?location schema:description ?description. FILTER(LANG(?description) = "it") }
              SERVICE wikibase:label { bd:serviceParam wikibase:language "it,en". }
            }
        """.trimIndent()
        return wikidata.filmingLocations(query).results.bindings.mapNotNull { binding ->
            val coordinates = parsePoint(binding.coord?.value) ?: return@mapNotNull null
            FilmLocation(
                name = binding.locationLabel?.value ?: "Location delle riprese",
                scene = "Scena specifica non indicata dalla fonte",
                realPlace = binding.locationLabel?.value ?: "Luogo non indicato",
                city = "",
                latitude = coordinates.second,
                longitude = coordinates.first,
                today = binding.description?.value ?: "Apri la mappa per vedere il luogo e le informazioni attuali."
            )
        }.distinctBy { "${it.latitude},${it.longitude}" }
    }

    private fun parsePoint(value: String?): Pair<Double, Double>? {
        val match = value?.let { Regex("Point\\(([-0-9.]+) ([-0-9.]+)\\)").find(it) } ?: return null
        val longitude = match.groupValues[1].toDoubleOrNull() ?: return null
        val latitude = match.groupValues[2].toDoubleOrNull() ?: return null
        return longitude to latitude
    }

    private fun clean(html: String?): String? = html?.let {
        Jsoup.parse(it).apply { select("sup, table, style, script, .mw-editsection").remove() }.text()
            .replace(Regex("\\s+"), " ").trim().takeIf { text -> text.isNotBlank() }
    }
}
