package com.filemanager.pro.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.filemanager.pro.R
import com.filemanager.pro.databinding.ActivityStorageAnalyzerBinding
import com.filemanager.pro.utils.FileManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StorageAnalyzerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStorageAnalyzerBinding
    private lateinit var fileManager: FileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageAnalyzerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileManager = FileManager(this)

        setupUI()
        analyzeStorage()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbarAnalyzer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Storage Analyzer"

        binding.toolbarAnalyzer.setNavigationOnClickListener {
            finish()
        }
    }

    private fun analyzeStorage() {
        binding.progressAnalyzing.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            val storageInfo = withContext(Dispatchers.IO) {
                fileManager.getStorageInfo()
            }

            val categories = withContext(Dispatchers.IO) {
                analyzeFileCategories()
            }

            withContext(Dispatchers.Main) {
                binding.progressAnalyzing.visibility = android.view.View.GONE
                displayStorageInfo(storageInfo)
                displayChart(categories)
            }
        }
    }

    private suspend fun analyzeFileCategories(): Map<String, Long> = withContext(Dispatchers.IO) {
        val categories = mutableMapOf(
            "Images" to 0L,
            "Videos" to 0L,
            "Audio" to 0L,
            "Documents" to 0L,
            "Archives" to 0L,
            "APKs" to 0L,
            "Others" to 0L
        )

        fun analyzeDirectory(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    analyzeDirectory(file)
                } else {
                    val size = file.length()
                    when (file.extension.lowercase()) {
                        "jpg", "jpeg", "png", "gif", "bmp", "webp" ->
                            categories["Images"] = categories["Images"]!! + size
                        "mp4", "mkv", "avi", "mov", "wmv" ->
                            categories["Videos"] = categories["Videos"]!! + size
                        "mp3", "wav", "ogg", "m4a", "flac" ->
                            categories["Audio"] = categories["Audio"]!! + size
                        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" ->
                            categories["Documents"] = categories["Documents"]!! + size
                        "zip", "rar", "7z", "tar", "gz" ->
                            categories["Archives"] = categories["Archives"]!! + size
                        "apk" ->
                            categories["APKs"] = categories["APKs"]!! + size
                        else ->
                            categories["Others"] = categories["Others"]!! + size
                    }
                }
            }
        }

        analyzeDirectory(Environment.getExternalStorageDirectory())
        categories
    }

    private fun displayStorageInfo(storageInfo: FileManager.StorageInfo) {
        binding.apply {
            textTotalStorage.text = formatSize(storageInfo.totalSpace)
            textUsedStorage.text = formatSize(storageInfo.usedSpace)
            textFreeStorage.text = formatSize(storageInfo.freeSpace)
            progressStorage.progress = storageInfo.usedPercentage
            textStoragePercentage.text = "${storageInfo.usedPercentage}% Used"
        }
    }

    private fun displayChart(categories: Map<String, Long>) {
        val entries = categories.filter { it.value > 0 }.map {
            PieEntry(it.value.toFloat(), it.key)
        }

        val dataSet = PieDataSet(entries, "Storage Usage").apply {
            colors = listOf(
                Color.rgb(255, 102, 102),
                Color.rgb(102, 178, 255),
                Color.rgb(178, 102, 255),
                Color.rgb(255, 178, 102),
                Color.rgb(102, 255, 178),
                Color.rgb(255, 204, 102),
                Color.rgb(153, 153, 153)
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }

        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            animateY(1000)
            invalidate()
        }
    }

    private fun formatSize(size: Long): String {
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
}