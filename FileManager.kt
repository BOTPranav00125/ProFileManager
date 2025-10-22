package com.filemanager.pro.utils

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.filemanager.pro.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileManager(private val context: Context) {

    suspend fun getFiles(directory: File, showHidden: Boolean = false): List<FileItem> =
        withContext(Dispatchers.IO) {
            try {
                directory.listFiles()
                    ?.filter { showHidden || !it.isHidden }
                    ?.map { FileItem(it) }
                    ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun searchFiles(
        directory: File,
        query: String,
        showHidden: Boolean = false
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FileItem>()
        searchRecursive(directory, query.lowercase(), showHidden, results)
        results
    }

    private fun searchRecursive(
        directory: File,
        query: String,
        showHidden: Boolean,
        results: MutableList<FileItem>
    ) {
        try {
            directory.listFiles()?.forEach { file ->
                if (showHidden || !file.isHidden) {
                    if (file.name.lowercase().contains(query)) {
                        results.add(FileItem(file))
                    }
                    if (file.isDirectory) {
                        searchRecursive(file, query, showHidden, results)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun copyFile(source: File, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (source.isDirectory) {
                FileUtils.copyDirectory(source, destination)
            } else {
                FileUtils.copyFile(source, destination)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun moveFile(source: File, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (source.isDirectory) {
                FileUtils.moveDirectory(source, destination)
            } else {
                FileUtils.moveFile(source, destination)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.isDirectory) {
                FileUtils.deleteDirectory(file)
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun renameFile(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val newFile = File(file.parent, newName)
            file.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createFolder(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(parent, name).mkdir()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createFile(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(parent, name).createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun compressFiles(files: List<File>, outputZip: File, password: String? = null): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val zipFile = if (password != null) {
                    ZipFile(outputZip, password.toCharArray())
                } else {
                    ZipFile(outputZip)
                }

                val zipParameters = ZipParameters().apply {
                    compressionMethod = CompressionMethod.DEFLATE
                    compressionLevel = CompressionLevel.NORMAL
                    if (password != null) {
                        isEncryptFiles = true
                    }
                }

                files.forEach { file ->
                    if (file.isDirectory) {
                        zipFile.addFolder(file, zipParameters)
                    } else {
                        zipFile.addFile(file, zipParameters)
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    suspend fun extractZip(zipFile: File, destination: File, password: String? = null): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val zip = if (password != null) {
                    ZipFile(zipFile, password.toCharArray())
                } else {
                    ZipFile(zipFile)
                }
                zip.extractAll(destination.absolutePath)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    fun getDirectorySize(directory: File): Long {
        return try {
            FileUtils.sizeOfDirectory(directory)
        } catch (e: Exception) {
            0L
        }
    }

    fun getStorageInfo(): StorageInfo {
        val externalStorage = Environment.getExternalStorageDirectory()
        val totalSpace = externalStorage.totalSpace
        val freeSpace = externalStorage.freeSpace
        val usedSpace = totalSpace - freeSpace

        return StorageInfo(
            totalSpace = totalSpace,
            freeSpace = freeSpace,
            usedSpace = usedSpace,
            usedPercentage = ((usedSpace.toDouble() / totalSpace) * 100).toInt()
        )
    }

    data class StorageInfo(
        val totalSpace: Long,
        val freeSpace: Long,
        val usedSpace: Long,
        val usedPercentage: Int
    )
}