package com.filemanager.pro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RecentFile::class, Favorite::class, FileOperation::class],
    version = 1,
    exportSchema = false
)
abstract class FileDatabase : RoomDatabase() {

    abstract fun recentFileDao(): RecentFileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun fileDao(): FileOperationDao

    companion object {
        @Volatile
        private var INSTANCE: FileDatabase? = null

        fun getDatabase(context: Context): FileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FileDatabase::class.java,
                    "file_manager_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}