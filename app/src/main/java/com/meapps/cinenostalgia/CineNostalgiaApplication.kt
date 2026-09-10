package com.meapps.cinenostalgia

import android.app.Application
import androidx.room.Room
import com.meapps.cinenostalgia.data.AppDatabase
import com.meapps.cinenostalgia.data.MovieRepository
import com.meapps.cinenostalgia.network.TmdbClient

class CineNostalgiaApplication : Application() {
    val database by lazy { Room.databaseBuilder(this, AppDatabase::class.java, "cinenostalgia.db").build() }
    val repository by lazy { MovieRepository(TmdbClient.api, database.favoriteDao(), BuildConfig.TMDB_API_KEY) }
}
