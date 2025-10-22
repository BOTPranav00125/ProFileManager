package com.filemanager.pro.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY timestamp DESC LIMIT 50")
    fun getAllRecentFiles(): LiveData<List<RecentFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(recentFile: RecentFile)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteRecentFile(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): LiveData<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE path = :path")
    suspend fun deleteFavorite(path: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE path = :path)")
    suspend fun isFavorite(path: String): Boolean
}

@Dao
interface FileOperationDao {
    @Query("SELECT * FROM file_operations ORDER BY timestamp DESC LIMIT 100")
    fun getAllOperations(): LiveData<List<FileOperation>>

    @Insert
    suspend fun insertOperation(operation: FileOperation)

    @Query("DELETE FROM file_operations")
    suspend fun clearAll()
}