package com.meapps.cinenostalgia.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY title") fun observeAll():Flow<List<FavoriteEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId=:id)") fun observeIsFavorite(id:Int):Flow<Boolean>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insert(item:FavoriteEntity)
    @Query("DELETE FROM favorites WHERE movieId=:id") suspend fun delete(id:Int)
}
@Database(entities=[FavoriteEntity::class],version=1,exportSchema=true)
abstract class AppDatabase:RoomDatabase(){abstract fun favoriteDao():FavoriteDao}
