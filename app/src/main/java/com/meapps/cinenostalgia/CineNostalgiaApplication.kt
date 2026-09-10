package com.meapps.cinenostalgia

import android.app.Application
import androidx.room.Room
import com.meapps.cinenostalgia.data.AppDatabase
import com.meapps.cinenostalgia.data.MovieRepository
import com.meapps.cinenostalgia.data.SettingsRepository
import com.meapps.cinenostalgia.data.KnowledgeRepository
import com.meapps.cinenostalgia.network.KnowledgeClient
import com.meapps.cinenostalgia.network.TmdbClient

class CineNostalgiaApplication : Application() {
    val database by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "cinenostalgia.db").build() }
    private val knowledgeRepository by lazy { KnowledgeRepository(KnowledgeClient.wikipedia, KnowledgeClient.wikidata) }
    val repository by lazy { MovieRepository(TmdbClient.api, database.favoriteDao(), knowledgeRepository, BuildConfig.TMDB_API_KEY) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
