package com.filemanager.pro.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.filemanager.pro.FileManagerApplication
import com.filemanager.pro.databinding.ActivityRecentFilesBinding
import com.filemanager.pro.ui.adapter.RecentFilesAdapter
import com.filemanager.pro.ui.viewmodel.FileViewModel
import com.filemanager.pro.ui.viewmodel.FileViewModelFactory
import java.io.File

class RecentFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecentFilesBinding
    private lateinit var viewModel: FileViewModel
    private lateinit var adapter: RecentFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupViewModel() {
        val repository = (application as FileManagerApplication).fileRepository
        viewModel = ViewModelProvider(
            this,
            FileViewModelFactory(repository, application)
        )[FileViewModel::class.java]

        viewModel.recentFiles.observe(this) { recentFiles ->
            adapter.submitList(recentFiles)

            if (recentFiles.isEmpty()) {
                binding.layoutEmpty.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
            } else {
                binding.layoutEmpty.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarRecent)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Recent Files"

        binding.toolbarRecent.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = RecentFilesAdapter { recentFile ->
            val file = File(recentFile.path)
            if (file.exists()) {
                openFile(file)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecentFilesActivity)
            adapter = this@RecentFilesActivity.adapter
        }
    }

    private fun openFile(file: File) {
        // Open file implementation
    }
}