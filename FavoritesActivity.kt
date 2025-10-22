package com.filemanager.pro.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.filemanager.pro.FileManagerApplication
import com.filemanager.pro.databinding.ActivityFavoritesBinding
import com.filemanager.pro.ui.adapter.FavoritesAdapter
import com.filemanager.pro.ui.viewmodel.FileViewModel
import com.filemanager.pro.ui.viewmodel.FileViewModelFactory
import java.io.File

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var viewModel: FileViewModel
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
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

        viewModel.favorites.observe(this) { favorites ->
            adapter.submitList(favorites)

            if (favorites.isEmpty()) {
                binding.layoutEmpty.visibility = android.view.View.VISIBLE
                binding.recyclerView.visibility = android.view.View.GONE
            } else {
                binding.layoutEmpty.visibility = android.view.View.GONE
                binding.recyclerView.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarFavorites)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favorites"

        binding.toolbarFavorites.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            onItemClick = { favorite ->
                val file = File(favorite.path)
                if (file.exists()) {
                    openFile(file)
                }
            },
            onRemoveClick = { favorite ->
                viewModel.removeFromFavorites(favorite.path)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = this@FavoritesActivity.adapter
        }
    }

    private fun openFile(file: File) {
        // Open file implementation
    }
}