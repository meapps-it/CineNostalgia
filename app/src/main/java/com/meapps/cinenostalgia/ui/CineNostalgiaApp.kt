package com.meapps.cinenostalgia.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clip
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.meapps.cinenostalgia.data.FilmLocation
import com.meapps.cinenostalgia.data.MovieDetail
import com.meapps.cinenostalgia.data.MovieSummary
import com.meapps.cinenostalgia.data.PersonRole
import com.meapps.cinenostalgia.ui.theme.MEColors

private enum class MainTab(val label: String) { HOME("Home"), SEARCH("Cerca"), FAVORITES("Preferiti") }

@Composable
fun CineNostalgiaApp(viewModel: CineNostalgiaViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(MainTab.HOME) }
    var credits by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MEHeader(
                canGoBack = state.detail != null || credits,
                onBack = { if (credits) credits = false else viewModel.closeMovie() },
                onCredits = { credits = true }
            )
        },
        bottomBar = {
            if (state.detail == null && !credits) MEBottomBar(tab = tab, onTab = { tab = it })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                credits -> CreditsScreen()
                state.detail != null -> DetailScreen(
                    detail = state.detail!!,
                    isFavorite = favorites.any { it.id == state.detail!!.summary.id },
                    onToggleFavorite = { viewModel.toggleFavorite(state.detail!!.summary, it) }
                )
                tab == MainTab.HOME -> HomeScreen(state, viewModel.apiReady, viewModel::updateQuery) {
                    tab = MainTab.SEARCH
                }
                tab == MainTab.SEARCH -> SearchScreen(state, viewModel::updateQuery, viewModel::openMovie)
                else -> FavoritesScreen(favorites, viewModel::openMovie)
            }
            if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = MEColors.Blue)
        }
    }
}

@Composable
private fun MEHeader(canGoBack: Boolean, onBack: () -> Unit, onCredits: () -> Unit) {
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
        MainTab.entries.forEach { item ->
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
private fun HomeScreen(state: MovieUiState, apiReady: Boolean, onQuery: (String) -> Unit, onSearch: () -> Unit) {
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
        item { SectionTitle("Viaggia nel tempo") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(listOf("Anni 70", "Anni 80", "Anni 90", "Anni 2000")) { decade ->
                    Text(decade, Modifier.background(Color.White, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SearchScreen(state: MovieUiState, onQuery: (String) -> Unit, onOpen: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchField(state.query, onQuery) }
        state.error?.let { item { Text(it, color = MEColors.Red) } }
        if (state.query.isNotBlank() && state.results.isEmpty() && !state.loading) item { MECard { Text("Nessun film trovato") } }
        items(state.results, key = { it.id }) { MovieResult(it) { onOpen(it.id) } }
    }
}

@Composable
private fun FavoritesScreen(movies: List<MovieSummary>, onOpen: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionTitle("I tuoi preferiti") }
        if (movies.isEmpty()) item { MECard { Text("Non hai ancora salvato film.", color = MEColors.SecondaryText) } }
        items(movies, key = { it.id }) { MovieResult(it) { onOpen(it.id) } }
    }
}

@Composable
private fun SearchField(value: String, onValue: (String) -> Unit, onSearch: () -> Unit = {}) {
    OutlinedTextField(
        value = value, onValueChange = onValue, modifier = Modifier.fillMaxWidth(), singleLine = true,
        placeholder = { Text("Cerca un film...") }, leadingIcon = { Icon(Icons.Default.Search, null) },
        shape = RoundedCornerShape(18.dp), keyboardActions = KeyboardActions(onSearch = { onSearch() })
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
            }
        }
    }
}

@Composable
private fun DetailScreen(detail: MovieDetail, isFavorite: Boolean, onToggleFavorite: (Boolean) -> Unit) {
    var spoilerVisible by remember(detail.summary.id) { mutableStateOf(false) }
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
                        Text(listOfNotNull(detail.runtime?.let { "$it min" }, detail.genres.joinToString().ifBlank { null }).joinToString(" · "), color = MEColors.SecondaryText, fontSize = 13.sp)
                        IconButton(onClick = { onToggleFavorite(isFavorite) }) {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Preferito", tint = if (isFavorite) MEColors.Red else MEColors.Blue)
                        }
                    }
                }
            }
        }
        item { DetailCard("Trama") { Text(detail.overview ?: "Informazione non disponibile") } }
        if (detail.cast.isNotEmpty()) item { CastSection(detail.cast, detail.summary.releaseDate) }
        if (detail.locations.isNotEmpty()) item { LocationsSection(detail.locations) }
        if (detail.curiosities.isNotEmpty()) item {
            DetailCard("Curiosità") { detail.curiosities.forEach { Text("• $it", modifier = Modifier.padding(bottom = 7.dp)) } }
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
    }
}

@Composable
private fun CastSection(cast: List<PersonRole>, releaseDate: String?) {
    DetailCard("Cast · Allora e oggi") {
        cast.forEach { person ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Poster(person.profileUrl, Modifier.size(70.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, fontWeight = FontWeight.Bold)
                    Text(person.character, color = MEColors.SecondaryText)
                    Text("Nel film: ${releaseDate?.let(person::ageAt)?.let { "$it anni" } ?: "dato non disponibile"}", fontSize = 13.sp)
                    Text(if (person.deathday != null) "Età alla morte: ${person.currentAge() ?: "dato non disponibile"}" else "Oggi: ${person.currentAge()?.let { "$it anni" } ?: "dato non disponibile"}", fontSize = 13.sp, color = MEColors.Green)
                    Text("Foto d'epoca non disponibile dalla fonte", fontSize = 11.sp, color = MEColors.SecondaryText)
                }
            }
        }
    }
}

@Composable
private fun LocationsSection(locations: List<FilmLocation>) {
    val context = LocalContext.current
    DetailCard("Location · Com'è oggi") {
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
