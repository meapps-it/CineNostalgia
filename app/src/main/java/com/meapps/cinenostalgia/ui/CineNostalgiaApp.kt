package com.meapps.cinenostalgia.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.meapps.cinenostalgia.BuildConfig
import com.meapps.cinenostalgia.data.FilmLocation
import com.meapps.cinenostalgia.data.MovieDetail
import com.meapps.cinenostalgia.data.MovieSummary
import com.meapps.cinenostalgia.data.PersonRole
import com.meapps.cinenostalgia.data.PersonDetail
import com.meapps.cinenostalgia.ui.theme.MEColors

private enum class MainTab(val label: String) { HOME("Home"), SEARCH("Cerca"), FAVORITES("Preferiti") }
private data class Decade(val label: String, val fromYear: Int, val toYear: Int)
private val decades = listOf(
    Decade("Anni 70", 1970, 1979), Decade("Anni 80", 1980, 1989),
    Decade("Anni 90", 1990, 1999), Decade("Anni 2000", 2000, 2009)
)

@Composable
fun CineNostalgiaApp(viewModel: CineNostalgiaViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(MainTab.HOME) }
    var credits by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }

    val navigateBack: () -> Unit = {
        when {
            state.personDetail != null -> viewModel.closePerson()
            credits -> credits = false
            settings -> settings = false
            state.detail != null -> viewModel.closeMovie()
            state.decadeLabel != null -> viewModel.closeDecade()
            tab != MainTab.HOME -> tab = MainTab.HOME
        }
    }
    val hasInternalBackStack = state.personDetail != null || credits || settings || state.detail != null || state.decadeLabel != null || tab != MainTab.HOME
    BackHandler(enabled = hasInternalBackStack, onBack = navigateBack)

    Scaffold(
        topBar = {
            MEHeader(
                canGoBack = state.personDetail != null || state.detail != null || state.decadeLabel != null || credits || settings,
                onBack = navigateBack,
                onCredits = { credits = true },
                onSettings = { settings = true }
            )
        },
        bottomBar = {
            if (state.personDetail == null && state.detail == null && state.decadeLabel == null && !credits && !settings) MEBottomBar(tab = tab, onTab = { tab = it })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.personDetail != null -> PersonDetailScreen(state.personDetail!!)
                credits -> CreditsScreen()
                settings -> SettingsScreen(fontScale, viewModel::setFontScale)
                state.detail != null -> DetailScreen(
                    detail = state.detail!!,
                    isFavorite = favorites.any { it.id == state.detail!!.summary.id && it.mediaType == state.detail!!.summary.mediaType },
                    onToggleFavorite = { viewModel.toggleFavorite(state.detail!!.summary, it) },
                    onPerson = viewModel::openPerson
                )
                state.decadeLabel != null -> DecadeScreen(
                    state = state,
                    onOpen = viewModel::openMovie,
                    onLoadMore = {
                        decades.firstOrNull { it.label == state.decadeLabel }?.let { viewModel.loadMoreDecade(it.fromYear, it.toYear) }
                    }
                )
                tab == MainTab.HOME -> HomeScreen(
                    state, viewModel.apiReady, viewModel::updateQuery,
                    onSearch = { tab = MainTab.SEARCH },
                    onDecade = { decade -> viewModel.openDecade(decade.label, decade.fromYear, decade.toYear) }
                )
                tab == MainTab.SEARCH -> SearchScreen(state, viewModel::updateQuery, viewModel::openMovie)
                else -> FavoritesScreen(favorites, viewModel::openMovie)
            }
            if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = MEColors.Blue)
        }
    }
}

@Composable
private fun MEHeader(canGoBack: Boolean, onBack: () -> Unit, onCredits: () -> Unit, onSettings: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().background(MEColors.Navy).statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canGoBack) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Indietro", tint = Color.White) }
        Column(Modifier.weight(1f)) {
            Text("CineNostalgia", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("Il cinema di ieri, da rivivere oggi", color = Color(0xFFCBD5E1), fontSize = 13.sp)
        }
        Box {
            IconButton(onClick = { expanded = true }, modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(14.dp))) {
                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Impostazioni") }, leadingIcon = { Icon(Icons.Default.Settings, null) }, onClick = {
                    expanded = false
                    onSettings()
                })
                DropdownMenuItem(text = { Text("Fonti e crediti") }, leadingIcon = { Icon(Icons.Default.Info, null) }, onClick = {
                    expanded = false
                    onCredits()
                })
            }
        }
    }
}

@Composable
private fun MEBottomBar(tab: MainTab, onTab: (MainTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MainTab.values().forEach { item ->
            val selected = item == tab
            Row(
                Modifier.weight(1f).clickable { onTab(item) }
                    .background(if (selected) MEColors.AmberSoft else Color.White, RoundedCornerShape(24.dp))
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (item) { MainTab.HOME -> Icons.Default.Home; MainTab.SEARCH -> Icons.Default.Search; MainTab.FAVORITES -> Icons.Default.Favorite },
                    null, Modifier.size(18.dp), tint = if (selected) MEColors.Amber else MEColors.SecondaryText
                )
                Spacer(Modifier.width(5.dp))
                Text(item.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HomeScreen(state: MovieUiState, apiReady: Boolean, onQuery: (String) -> Unit, onSearch: () -> Unit, onDecade: (Decade) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            MECard {
                Text("Che film vuoi rivivere?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SearchField(state.query, onQuery, onSearch)
                if (!apiReady) Text("Modalità demo offline · configura TMDB per cercare tutto il catalogo", color = MEColors.SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item { SectionTitle("Film da riscoprire") }
        item { PosterRow(state.featured) { onQuery(it.title); onSearch() } }
        if (state.featuredSeries.isNotEmpty()) {
            item { SectionTitle("Serie TV da riscoprire") }
            item { PosterRow(state.featuredSeries) { onQuery(it.title); onSearch() } }
        }
        item { SectionTitle("Viaggia nel tempo") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(decades) { decade ->
                    Text(decade.label, Modifier.clickable { onDecade(decade) }.background(Color.White, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DecadeScreen(state: MovieUiState, onOpen: (MovieSummary) -> Unit, onLoadMore: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle(state.decadeLabel ?: "Film") }
        if (state.decadeMovies.isEmpty() && !state.loading) {
            item { MECard { Text("Configura TMDB per caricare l'intero catalogo.", color = MEColors.SecondaryText) } }
        }
        items(state.decadeMovies, key = { "${it.mediaType}:${it.id}" }) { MovieResult(it) { onOpen(it) } }
        if (state.canLoadMore && state.decadeMovies.isNotEmpty()) {
            item {
                Button(onClick = onLoadMore, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MEColors.Blue)) {
                    Text("Carica altri film")
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(fontScale: Float, onFontScaleChange: (Float) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Impostazioni") }
        item {
            DetailCard("Dimensione testo") {
                Text("Regola i testi dell'app", color = MEColors.SecondaryText)
                Slider(
                    value = fontScale,
                    onValueChange = onFontScaleChange,
                    valueRange = 0.80f..1.40f,
                    steps = 5
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Piccolo", fontSize = 12.sp)
                    Text("${(fontScale * 100).toInt()}%", color = MEColors.Blue, fontWeight = FontWeight.Bold)
                    Text("Grande", fontSize = 18.sp)
                }
                Text("Il cursore si aggiunge alla dimensione scelta nelle impostazioni Samsung.", fontSize = 12.sp, color = MEColors.SecondaryText, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item { DetailCard("Informazioni") { Text("CineNostalgia ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Bold) } }
    }
}

@Composable
private fun SearchScreen(state: MovieUiState, onQuery: (String) -> Unit, onOpen: (MovieSummary) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchField(state.query, onQuery) }
        state.error?.let { item { Text(it, color = MEColors.Red) } }
        if (state.query.isNotBlank() && state.results.isEmpty() && !state.loading) item { MECard { Text("Nessun film trovato") } }
        items(state.results, key = { "${it.mediaType}:${it.id}" }) { MovieResult(it) { onOpen(it) } }
    }
}

@Composable
private fun FavoritesScreen(movies: List<MovieSummary>, onOpen: (MovieSummary) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle("I tuoi preferiti") }
        if (movies.isEmpty()) item { MECard { Text("Non hai ancora salvato film.", color = MEColors.SecondaryText) } }
        items(movies, key = { "${it.mediaType}:${it.id}" }) { MovieResult(it) { onOpen(it) } }
    }
}

@Composable
private fun SearchField(value: String, onValue: (String) -> Unit, onSearch: () -> Unit = {}) {
    OutlinedTextField(
        value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(), singleLine = true,
        placeholder = { Text("Cerca film o attore...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Avvia ricerca") } },
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun PosterRow(movies: List<MovieSummary>, onOpen: (MovieSummary) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(movies, key = { it.id }) { movie ->
            Column(Modifier.width(138.dp).clickable { onOpen(movie) }) {
                Poster(movie.posterUrl, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
                Text(movie.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                Text(movie.year, color = MEColors.SecondaryText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MovieResult(movie: MovieSummary, onClick: () -> Unit) {
    MECard(Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Poster(movie.posterUrl, Modifier.width(72.dp).aspectRatio(2f / 3f))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (movie.originalTitle != movie.title) Text(movie.originalTitle, color = MEColors.SecondaryText)
                Text(movie.year.ifBlank { "Anno non disponibile" }, color = MEColors.Blue, fontWeight = FontWeight.Bold)
                Text(if (movie.mediaType == "tv") "SERIE TV" else "FILM", color = MEColors.Amber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun DetailScreen(detail: MovieDetail, isFavorite: Boolean, onToggleFavorite: (Boolean) -> Unit, onPerson: (Int) -> Unit) {
    var spoilerVisible by remember(detail.summary.id) { mutableStateOf(false) }
    val movieMetadata = buildList {
        detail.runtime?.let { add("$it min") }
        if (detail.genres.isNotEmpty()) add(detail.genres.joinToString())
    }.joinToString(" · ")
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            MECard {
                Row {
                    Poster(detail.summary.posterUrl, Modifier.width(116.dp).aspectRatio(2f / 3f))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(detail.summary.title, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text(detail.summary.originalTitle, color = MEColors.SecondaryText)
                        Text(detail.summary.year, color = MEColors.Blue, fontWeight = FontWeight.Bold)
                        Text(detail.director ?: "Regista non disponibile", modifier = Modifier.padding(top = 8.dp))
                        if (movieMetadata.isNotBlank()) {
                            Text(movieMetadata, color = MEColors.SecondaryText, fontSize = 13.sp)
                        }
                        IconButton(onClick = { onToggleFavorite(isFavorite) }) {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Preferito", tint = if (isFavorite) MEColors.Red else MEColors.Blue)
                        }
                    }
                }
            }
        }
        item { DetailCard("Trama estesa") { Text(detail.overview ?: "Informazione non disponibile") } }
        item { CastSection(detail.cast, detail.summary.releaseDate, onPerson) }
        item { LocationsSection(detail.locations) }
        item {
            DetailCard("Curiosità") {
                if (detail.curiosities.isEmpty()) {
                    Text("Informazione non disponibile dalle fonti collegate.", color = MEColors.SecondaryText)
                } else {
                    detail.curiosities.forEach { Text("• $it", modifier = Modifier.padding(bottom = 7.dp)) }
                }
            }
        }
        item {
            DetailCard("Spoiler") {
                if (!spoilerVisible) Button(onClick = { spoilerVisible = true }, colors = ButtonDefaults.buttonColors(containerColor = MEColors.Blue)) { Text("Mostra spoiler") }
                else Text(detail.spoiler ?: "Informazione non disponibile")
            }
        }
        item {
            DetailCard("Dove vederlo") {
                if (detail.providers.isEmpty()) Text("Disponibilità non rilevata. I provider dipendono dai dati TMDB per l'Italia.", color = MEColors.SecondaryText)
                else detail.providers.forEach { Text("• ${it.name}", modifier = Modifier.padding(bottom = 5.dp)) }
            }
        }
        if (detail.sources.isNotEmpty()) item {
            DetailCard("Fonti della scheda") {
                detail.sources.forEach { Text("• $it", color = MEColors.SecondaryText, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun CastSection(cast: List<PersonRole>, releaseDate: String?, onPerson: (Int) -> Unit) {
    DetailCard("Cast · Allora e oggi") {
        if (cast.isEmpty()) Text("Informazione non disponibile dalle fonti collegate.", color = MEColors.SecondaryText)
        cast.forEach { person ->
            Row(Modifier.fillMaxWidth().clickable { onPerson(person.id) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Poster(person.profileUrl, Modifier.size(70.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, fontWeight = FontWeight.Bold)
                    Text(person.character, color = MEColors.SecondaryText)
                    val ageAtRelease = releaseDate?.let { date -> person.ageAt(date) }
                    Text("Nel film: ${ageAtRelease?.let { "$it anni" } ?: "dato non disponibile"}", fontSize = 13.sp)
                    Text(if (person.deathday != null) "Età alla morte: ${person.currentAge() ?: "dato non disponibile"}" else "Oggi: ${person.currentAge()?.let { "$it anni" } ?: "dato non disponibile"}", fontSize = 13.sp, color = MEColors.Green)
                    Text("Foto d'epoca non disponibile dalla fonte", fontSize = 11.sp, color = MEColors.SecondaryText)
                    Text("Tocca per aprire la scheda completa", fontSize = 11.sp, color = MEColors.Blue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PersonDetailScreen(person: PersonDetail) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            MECard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Poster(person.profileUrl, Modifier.size(110.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(person.name, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        person.knownFor?.let { Text(it, color = MEColors.Blue) }
                        Text("Nascita: ${person.birthday ?: "non disponibile"}", fontSize = 13.sp)
                        person.deathday?.let { Text("Morte: $it", fontSize = 13.sp) }
                        person.currentAge()?.let { age -> Text(if (person.deathday == null) "Età attuale: $age anni" else "Età alla morte: $age anni", color = MEColors.Green, fontSize = 13.sp) }
                        person.placeOfBirth?.let { Text(it, color = MEColors.SecondaryText, fontSize = 13.sp) }
                    }
                }
            }
        }
        item { DetailCard("Biografia e storia") { Text(person.biography ?: "Informazione non disponibile dalle fonti collegate.") } }
        item {
            DetailCard("Film e serie principali") {
                if (person.filmography.isEmpty()) Text("Filmografia non disponibile.", color = MEColors.SecondaryText)
                person.filmography.take(30).forEach { title ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(title.title, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Text("${title.year} · ${if (title.mediaType == "tv") "Serie" else "Film"}", color = MEColors.SecondaryText, fontSize = 12.sp)
                    }
                }
            }
        }
        person.wikipediaSource?.let { source -> item { DetailCard("Fonte") { Text(source, color = MEColors.SecondaryText) } } }
    }
}

@Composable
private fun LocationsSection(locations: List<FilmLocation>) {
    val context = LocalContext.current
    DetailCard("Location · Com'è oggi") {
        if (locations.isEmpty()) {
            Text("Location e confronto con il presente non disponibili dalle fonti collegate.", color = MEColors.SecondaryText)
        }
        locations.forEach { location ->
            Text(location.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text("Nel film: ${location.scene}", color = MEColors.SecondaryText)
            Text("Luogo reale: ${location.realPlace} · ${location.city}")
            Text(location.today, modifier = Modifier.padding(vertical = 5.dp))
            TextButton(onClick = {
                val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${Uri.encode(location.realPlace)})")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }) { Icon(Icons.Default.LocationOn, null); Text("Apri sulla mappa") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CreditsScreen() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("Fonti e crediti") }
        item { DetailCard("Dati cinematografici") { Text("Dati e immagini forniti da TMDB. Questa applicazione usa l'API TMDB ma non è approvata o certificata da TMDB.") } }
        item { DetailCard("Approfondimenti") { Text("Architettura predisposta per Wikidata, Wikipedia e immagini con licenza verificata da Wikimedia Commons. Le location demo sono curate editorialmente e non vengono ottenute tramite scraping.") } }
        item { DetailCard("Mappe") { Text("I luoghi vengono aperti con un'app mappe installata sul dispositivo. Nessuna chiave Google Maps è richiesta nell'MVP.") } }
        item { DetailCard("Licenze") { Text("Prima di monetizzare l'app occorre verificare le condizioni commerciali delle fonti e conservare attribuzione e licenza di ogni immagine.") } }
    }
}

@Composable
private fun Poster(url: String?, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
        if (url == null) Text("Immagine\nnon disponibile", color = MEColors.SecondaryText, fontSize = 11.sp)
        else AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

@Composable
private fun MECard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MEColors.Border), elevation = CardDefaults.cardElevation(1.dp)
    ) { Column(Modifier.padding(16.dp), content = content) }
}

@Composable private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) = MECard { SectionTitle(title); Spacer(Modifier.height(8.dp)); content() }
@Composable private fun SectionTitle(title: String) { Text(title.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface) }
