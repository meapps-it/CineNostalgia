package com.meapps.cinenostalgia.network

import retrofit2.http.GET
import retrofit2.http.Query

data class WikipediaSearchResponse(val query: WikipediaSearchQuery?)
data class WikipediaSearchQuery(val search: List<WikipediaSearchItem> = emptyList())
data class WikipediaSearchItem(val title: String)
data class WikipediaParseResponse(val parse: WikipediaParsePage?)
data class WikipediaParsePage(
    val title: String = "",
    val sections: List<WikipediaSection> = emptyList(),
    val text: String? = null
)
data class WikipediaSection(val index: String, val line: String)

interface WikipediaApi {
    @GET("w/api.php")
    suspend fun search(
        @Query("action") action: String = "query",
        @Query("list") list: String = "search",
        @Query("srsearch") query: String,
        @Query("srlimit") limit: Int = 1,
        @Query("format") format: String = "json"
    ): WikipediaSearchResponse

    @GET("w/api.php")
    suspend fun sections(
        @Query("action") action: String = "parse",
        @Query("page") page: String,
        @Query("prop") property: String = "sections",
        @Query("formatversion") formatVersion: Int = 2,
        @Query("format") format: String = "json"
    ): WikipediaParseResponse

    @GET("w/api.php")
    suspend fun sectionText(
        @Query("action") action: String = "parse",
        @Query("page") page: String,
        @Query("prop") property: String = "text",
        @Query("section") section: String,
        @Query("formatversion") formatVersion: Int = 2,
        @Query("format") format: String = "json"
    ): WikipediaParseResponse
}

data class SparqlResponse(val results: SparqlResults = SparqlResults())
data class SparqlResults(val bindings: List<SparqlBinding> = emptyList())
data class SparqlValue(val value: String = "")
data class SparqlBinding(
    val location: SparqlValue? = null,
    val locationLabel: SparqlValue? = null,
    val coord: SparqlValue? = null,
    val description: SparqlValue? = null
)

interface WikidataApi {
    @GET("sparql")
    suspend fun filmingLocations(
        @Query("query") query: String,
        @Query("format") format: String = "json"
    ): SparqlResponse
}
