package com.filemanager.pro.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.filemanager.pro.databinding.ActivityImageViewerBinding
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

class ImageViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewerBinding
    private var isControlsVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("file_path") ?: run {
            finish()
            return
        }

        val imageFile = File(filePath)
        if (!imageFile.exists()) {
            finish()
            return
        }

        setupUI(imageFile)
    }

    private fun setupUI(imageFile: File) {
        // Set title
        binding.toolbarImage.title = imageFile.name
        setSupportActionBar(binding.toolbarImage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarImage.setNavigationOnClickListener {
            finish()
        }

        // Load image
        Glide.with(this)
            .load(imageFile)
            .into(binding.photoView)

        // Toggle controls on tap
        binding.photoView.setOnClickListener {
            toggleControls()
        }

        // File info
        binding.textImageInfo.text = buildString {
            append("Size: ${formatFileSize(imageFile.length())}
                ")
                        append("Path: ${imageFile.absolutePath}")
        }
    }

    private fun toggleControls() {
        isControlsVisible = !isControlsVisible

        binding.toolbarImage.visibility = if (isControlsVisible) View.VISIBLE else View.GONE
        binding.layoutImageInfo.visibility = if (isControlsVisible) View.VISIBLE else View.GONE

        // Hide system UI
        if (!isControlsVisible) {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun formatFileSize(size: Long): String {
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