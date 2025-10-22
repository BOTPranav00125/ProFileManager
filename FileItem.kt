package com.filemanager.pro.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0 else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension,
    val mimeType: String = getMimeType(file),
    val isHidden: Boolean = file.isHidden,
    var isSelected: Boolean = false,
    val canRead: Boolean = file.canRead(),
    val canWrite: Boolean = file.canWrite()
) : Parcelable {

    companion object {
        fun getMimeType(file: File): String {
            if (file.isDirectory) return "directory"

            return when (file.extension.lowercase()) {
                // Images
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg" -> "image"
                // Videos
                "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "3gp" -> "video"
                // Audio
                "mp3", "wav", "ogg", "m4a", "flac", "aac", "wma" -> "audio"
                // Documents
                "pdf" -> "pdf"
                "doc", "docx" -> "document"
                "xls", "xlsx" -> "spreadsheet"
                "ppt", "pptx" -> "presentation"
                "txt", "log" -> "text"
                // Archives
                "zip", "rar", "7z", "tar", "gz", "bz2" -> "archive"
                // Code
                "java", "kt", "xml", "json", "html", "css", "js", "py", "cpp", "c", "h" -> "code"
                // APK
                "apk" -> "apk"
                else -> "unknown"
            }
        }
    }

    fun getFormattedSize(): String {
        if (isDirectory) return "--"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            size >= gb -> String.format("%.2f GB", size / gb)
            size >= mb -> String.format("%.2f MB", size / mb)
            size >= kb -> String.format("%.2f KB", size / kb)
            else -> "$size B"
        }
    }

    fun getFileType(): FileType {
        return when (mimeType) {
            "directory" -> FileType.FOLDER
            "image" -> FileType.IMAGE
            "video" -> FileType.VIDEO
            "audio" -> FileType.AUDIO
            "pdf" -> FileType.PDF
            "document", "spreadsheet", "presentation" -> FileType.DOCUMENT
            "archive" -> FileType.ARCHIVE
            "apk" -> FileType.APK
            "code" -> FileType.CODE
            "text" -> FileType.TEXT
            else -> FileType.UNKNOWN
        }
    }
}

enum class FileType {
    FOLDER, IMAGE, VIDEO, AUDIO, PDF, DOCUMENT, ARCHIVE, APK, CODE, TEXT, UNKNOWN
}