package com.filemanager.pro.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.filemanager.pro.databinding.ActivityVideoPlayerBinding
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentPosition = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("file_path") ?: run {
            finish()
            return
        }

        val videoFile = File(filePath)
        if (!videoFile.exists()) {
            finish()
            return
        }

        setupUI(videoFile)
    }

    private fun setupUI(videoFile: File) {
        binding.toolbarVideo.title = videoFile.name
        setSupportActionBar(binding.toolbarVideo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarVideo.setNavigationOnClickListener {
            finish()
        }

        initializePlayer(videoFile)
    }

    private fun initializePlayer(videoFile: File) {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            val mediaItem = MediaItem.fromUri(videoFile.toURI().toString())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.playWhenReady = playWhenReady
            exoPlayer.seekTo(currentPosition)
            exoPlayer.prepare()
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            currentPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
        player = null
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            intent.getStringExtra("file_path")?.let { path ->
                initializePlayer(File(path))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}