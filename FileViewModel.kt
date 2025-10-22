package com.filemanager.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.filemanager.pro.data.database.Favorite
import com.filemanager.pro.data.database.RecentFile
import com.filemanager.pro.data.model.FileItem
import com.filemanager.pro.data.repository.FileRepository
import com.filemanager.pro.utils.FileManager
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel(
    private val repository: FileRepository,
    application: Application
) : AndroidViewModel(application) {

    private val fileManager = FileManager(application)

    private val _files = MutableLiveData<List<FileItem>>()
    val files: LiveData<List<FileItem>> = _files

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentPath = MutableLiveData<File>()
    val currentPath: LiveData<File> = _currentPath

    private val _selectionMode = MutableLiveData<Boolean>(false)
    val selectionMode: LiveData<Boolean> = _selectionMode

    private val _selectedFiles = MutableLiveData<MutableList<FileItem>>(mutableListOf())
    val selectedFiles: LiveData<MutableList<FileItem>> = _selectedFiles

    val recentFiles: LiveData<List<RecentFile>> = repository.recentFiles
    val favorites: LiveData<List<Favorite>> = repository.favorites

    fun loadFiles(directory: File, showHidden: Boolean = false) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                _currentPath.value = directory

                val fileList = fileManager.getFiles(directory, showHidden)
                _files.value = fileList
            } catch (e: Exception) {
                _error.value = "Error loading files: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchFiles(directory: File, query: String, showHidden: Boolean = false) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val results = fileManager.searchFiles(directory, query, showHidden)
                _files.value = results
            } catch (e: Exception) {
                _error.value = "Search error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun sortFiles(sortBy: String, ascending: Boolean = true) {
        val currentFiles = _files.value ?: return

        val sorted = when (sortBy) {
            "name" -> currentFiles.sortedWith(
                if (ascending) compareBy({ !it.isDirectory }, { it.name.lowercase() })
                else compareByDescending<FileItem> { !it.isDirectory }.thenByDescending { it.name.lowercase() }
            )
            "size" -> currentFiles.sortedWith(
                if (ascending) compareBy({ !it.isDirectory }, { it.size })
                else compareByDescending<FileItem> { !it.isDirectory }.thenByDescending { it.size }
            )
            "date" -> currentFiles.sortedWith(
                if (ascending) compareBy({ !it.isDirectory }, { it.lastModified })
                else compareByDescending<FileItem> { !it.isDirectory }.thenByDescending { it.lastModified }
            )
            "type" -> currentFiles.sortedWith(
                if (ascending) compareBy({ !it.isDirectory }, { it.extension.lowercase() })
                else compareByDescending<FileItem> { !it.isDirectory }.thenByDescending { it.extension.lowercase() }
            )
            else -> currentFiles
        }

        _files.value = sorted
    }

    fun copyFiles(sources: List<File>, destination: File, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                var allSuccess = true
                sources.forEach { source ->
                    val destFile = File(destination, source.name)
                    val success = fileManager.copyFile(source, destFile)
                    if (!success) allSuccess = false
                }
                callback(allSuccess, if (allSuccess) "Files copied successfully" else "Some files failed to copy")
            } catch (e: Exception) {
                callback(false, "Copy failed: ${e.message}")
            }
        }
    }

    fun moveFiles(sources: List<File>, destination: File, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                var allSuccess = true
                sources.forEach { source ->
                    val destFile = File(destination, source.name)
                    val success = fileManager.moveFile(source, destFile)
                    if (!success) allSuccess = false
                }
                callback(allSuccess, if (allSuccess) "Files moved successfully" else "Some files failed to move")
            } catch (e: Exception) {
                callback(false, "Move failed: ${e.message}")
            }
        }
    }

    fun deleteFiles(files: List<File>, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                var allSuccess = true
                files.forEach { file ->
                    val success = fileManager.deleteFile(file)
                    if (!success) allSuccess = false
                }
                callback(allSuccess, if (allSuccess) "Files deleted successfully" else "Some files failed to delete")
            } catch (e: Exception) {
                callback(false, "Delete failed: ${e.message}")
            }
        }
    }

    fun renameFile(file: File, newName: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = fileManager.renameFile(file, newName)
                callback(success, if (success) "File renamed successfully" else "Failed to rename file")
            } catch (e: Exception) {
                callback(false, "Rename failed: ${e.message}")
            }
        }
    }

    fun createFolder(parent: File, name: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = fileManager.createFolder(parent, name)
                callback(success, if (success) "Folder created successfully" else "Failed to create folder")
            } catch (e: Exception) {
                callback(false, "Create folder failed: ${e.message}")
            }
        }
    }

    fun createFile(parent: File, name: String, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = fileManager.createFile(parent, name)
                callback(success, if (success) "File created successfully" else "Failed to create file")
            } catch (e: Exception) {
                callback(false, "Create file failed: ${e.message}")
            }
        }
    }

    fun compressFiles(files: List<File>, outputZip: File, password: String? = null, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = fileManager.compressFiles(files, outputZip, password)
                callback(success, if (success) "Files compressed successfully" else "Failed to compress files")
            } catch (e: Exception) {
                callback(false, "Compress failed: ${e.message}")
            }
        }
    }

    fun extractZip(zipFile: File, destination: File, password: String? = null, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = fileManager.extractZip(zipFile, destination, password)
                callback(success, if (success) "Files extracted successfully" else "Failed to extract files")
            } catch (e: Exception) {
                callback(false, "Extract failed: ${e.message}")
            }
        }
    }

    fun addToRecent(fileItem: FileItem) {
        viewModelScope.launch {
            val recentFile = RecentFile(
                path = fileItem.path,
                name = fileItem.name,
                timestamp = System.currentTimeMillis(),
                fileType = fileItem.mimeType,
                size = fileItem.size
            )
            repository.addRecentFile(recentFile)
        }
    }

    fun addToFavorites(fileItem: FileItem) {
        viewModelScope.launch {
            val favorite = Favorite(
                path = fileItem.path,
                name = fileItem.name,
                addedAt = System.currentTimeMillis(),
                fileType = fileItem.mimeType
            )
            repository.addFavorite(favorite)
        }
    }

    fun removeFromFavorites(path: String) {
        viewModelScope.launch {
            repository.removeFavorite(path)
        }
    }

    suspend fun isFavorite(path: String): Boolean {
        return repository.isFavorite(path)
    }

    fun toggleSelection(fileItem: FileItem) {
        val currentSelected = _selectedFiles.value ?: mutableListOf()

        if (currentSelected.contains(fileItem)) {
            currentSelected.remove(fileItem)
        } else {
            currentSelected.add(fileItem)
        }

        _selectedFiles.value = currentSelected
        _selectionMode.value = currentSelected.isNotEmpty()
    }

    fun selectAll() {
        _selectedFiles.value = _files.value?.toMutableList() ?: mutableListOf()
        _selectionMode.value = true
    }

    fun clearSelection() {
        _selectedFiles.value = mutableListOf()
        _selectionMode.value = false
    }

    fun getStorageInfo() = fileManager.getStorageInfo()
}

class FileViewModelFactory(
    private val repository: FileRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FileViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}