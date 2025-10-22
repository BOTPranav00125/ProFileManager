package com.filemanager.pro.data.repository

import androidx.lifecycle.LiveData
import com.filemanager.pro.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileRepository(
    private val fileOperationDao: FileOperationDao,
    private val recentFileDao: RecentFileDao,
    private val favoriteDao: FavoriteDao
) {

    // Recent Files
    val recentFiles: LiveData<List<RecentFile>> = recentFileDao.getAllRecentFiles()

    suspend fun addRecentFile(recentFile: RecentFile) = withContext(Dispatchers.IO) {
        recentFileDao.insertRecentFile(recentFile)
    }

    suspend fun removeRecentFile(path: String) = withContext(Dispatchers.IO) {
        recentFileDao.deleteRecentFile(path)
    }

    suspend fun clearRecentFiles() = withContext(Dispatchers.IO) {
        recentFileDao.clearAll()
    }

    // Favorites
    val favorites: LiveData<List<Favorite>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(favorite: Favorite) = withContext(Dispatchers.IO) {
        favoriteDao.insertFavorite(favorite)
    }

    suspend fun removeFavorite(path: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavorite(path)
    }

    suspend fun isFavorite(path: String): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.isFavorite(path)
    }

    // File Operations
    val fileOperations: LiveData<List<FileOperation>> = fileOperationDao.getAllOperations()

    suspend fun addFileOperation(operation: FileOperation) = withContext(Dispatchers.IO) {
        fileOperationDao.insertOperation(operation)
    }

    suspend fun clearFileOperations() = withContext(Dispatchers.IO) {
        fileOperationDao.clearAll()
    }
}