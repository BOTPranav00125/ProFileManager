package com.filemanager.pro.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.filemanager.pro.FileManagerApplication
import com.filemanager.pro.R
import com.filemanager.pro.utils.FileManager
import kotlinx.coroutines.*
import java.io.File

class FileOperationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fileManager: FileManager
    private var notificationId = 1000

    override fun onCreate() {
        super.onCreate()
        fileManager = FileManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val operation = it.getStringExtra(EXTRA_OPERATION) ?: return START_NOT_STICKY
            val sourcePaths = it.getStringArrayListExtra(EXTRA_SOURCE_PATHS) ?: return START_NOT_STICKY
            val destinationPath = it.getStringExtra(EXTRA_DESTINATION_PATH)

            when (operation) {
                OPERATION_COPY -> performCopy(sourcePaths, destinationPath, startId)
                OPERATION_MOVE -> performMove(sourcePaths, destinationPath, startId)
                OPERATION_DELETE -> performDelete(sourcePaths, startId)
                OPERATION_COMPRESS -> performCompress(sourcePaths, destinationPath, startId)
                OPERATION_EXTRACT -> performExtract(sourcePaths.first(), destinationPath, startId)
            }
        }

        return START_NOT_STICKY
    }

    private fun performCopy(sourcePaths: List<String>, destinationPath: String?, startId: Int) {
        if (destinationPath == null) {
            stopSelf(startId)
            return
        }

        val notification = createNotification("Copying files...", 0, sourcePaths.size)
        startForeground(notificationId, notification)

        serviceScope.launch {
            var completed = 0
            val destination = File(destinationPath)

            sourcePaths.forEach { sourcePath ->
                val source = File(sourcePath)
                val destFile = File(destination, source.name)

                fileManager.copyFile(source, destFile)
                completed++

                updateNotification("Copying files...", completed, sourcePaths.size)
            }

            showCompletionNotification("Copy complete", "$completed files copied")
            stopForeground(true)
            stopSelf(startId)
        }
    }

    private fun performMove(sourcePaths: List<String>, destinationPath: String?, startId: Int) {
        if (destinationPath == null) {
            stopSelf(startId)
            return
        }

        val notification = createNotification("Moving files...", 0, sourcePaths.size)
        startForeground(notificationId, notification)

        serviceScope.launch {
            var completed = 0
            val destination = File(destinationPath)

            sourcePaths.forEach { sourcePath ->
                val source = File(sourcePath)
                val destFile = File(destination, source.name)

                fileManager.moveFile(source, destFile)
                completed++

                updateNotification("Moving files...", completed, sourcePaths.size)
            }

            showCompletionNotification("Move complete", "$completed files moved")
            stopForeground(true)
            stopSelf(startId)
        }
    }

    private fun performDelete(sourcePaths: List<String>, startId: Int) {
        val notification = createNotification("Deleting files...", 0, sourcePaths.size)
        startForeground(notificationId, notification)

        serviceScope.launch {
            var completed = 0

            sourcePaths.forEach { sourcePath ->
                val source = File(sourcePath)
                fileManager.deleteFile(source)
                completed++

                updateNotification("Deleting files...", completed, sourcePaths.size)
            }

            showCompletionNotification("Delete complete", "$completed files deleted")
            stopForeground(true)
            stopSelf(startId)
        }
    }

    private fun performCompress(sourcePaths: List<String>, destinationPath: String?, startId: Int) {
        if (destinationPath == null) {
            stopSelf(startId)
            return
        }

        val notification = createNotification("Compressing files...", 0, 100)
        startForeground(notificationId, notification)

        serviceScope.launch {
            val files = sourcePaths.map { File(it) }
            val outputZip = File(destinationPath)

            fileManager.compressFiles(files, outputZip)

            showCompletionNotification("Compression complete", "Archive created")
            stopForeground(true)
            stopSelf(startId)
        }
    }

    private fun performExtract(sourcePath: String, destinationPath: String?, startId: Int) {
        if (destinationPath == null) {
            stopSelf(startId)
            return
        }

        val notification = createNotification("Extracting files...", 0, 100)
        startForeground(notificationId, notification)

        serviceScope.launch {
            val zipFile = File(sourcePath)
            val destination = File(destinationPath)

            fileManager.extractZip(zipFile, destination)

            showCompletionNotification("Extraction complete", "Files extracted")
            stopForeground(true)
            stopSelf(startId)
        }
    }

    private fun createNotification(title: String, progress: Int, max: Int): Notification {
        val builder = NotificationCompat.Builder(this, FileManagerApplication.CHANNEL_FILE_OPERATIONS)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_folder)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        if (max > 0) {
            builder.setProgress(max, progress, false)
                .setContentText("$progress / $max")
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }

    private fun updateNotification(title: String, progress: Int, max: Int) {
        val notification = createNotification(title, progress, max)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }

    private fun showCompletionNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(this, FileManagerApplication.CHANNEL_FILE_OPERATIONS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_folder)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId++, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val EXTRA_OPERATION = "extra_operation"
        const val EXTRA_SOURCE_PATHS = "extra_source_paths"
        const val EXTRA_DESTINATION_PATH = "extra_destination_path"

        const val OPERATION_COPY = "copy"
        const val OPERATION_MOVE = "move"
        const val OPERATION_DELETE = "delete"
        const val OPERATION_COMPRESS = "compress"
        const val OPERATION_EXTRACT = "extract"
    }
}