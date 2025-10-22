package com.filemanager.pro.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.filemanager.pro.FileManagerApplication
import com.filemanager.pro.R
import com.filemanager.pro.databinding.ActivityMainBinding
import com.filemanager.pro.ui.adapter.FileAdapter
import com.filemanager.pro.ui.adapter.NavigationAdapter
import com.filemanager.pro.ui.viewmodel.FileViewModel
import com.filemanager.pro.ui.viewmodel.FileViewModelFactory
import com.filemanager.pro.utils.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: FileViewModel
    private lateinit var fileAdapter: FileAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var currentPath: File = Environment.getExternalStorageDirectory()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadFiles(currentPath)
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = (application as FileManagerApplication).preferenceManager

        setupViewModel()
        setupToolbar()
        setupDrawer()
        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()

        checkPermissions()
    }

    private fun setupViewModel() {
        val repository = (application as FileManagerApplication).fileRepository
        viewModel = ViewModelProvider(
            this,
            FileViewModelFactory(repository, application)
        )[FileViewModel::class.java]

        viewModel.files.observe(this) { files ->
            fileAdapter.submitList(files)
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateToolbarTitle()
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigationView()
    }

    private fun setupNavigationView() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_internal_storage -> {
                    navigateToPath(Environment.getExternalStorageDirectory())
                }
                R.id.nav_downloads -> {
                    navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                }
                R.id.nav_images -> {
                    navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                }
                R.id.nav_videos -> {
                    navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
                }
                R.id.nav_audio -> {
                    navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
                }
                R.id.nav_documents -> {
                    navigateToPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))
                }
                R.id.nav_recent -> {
                    startActivity(Intent(this, RecentFilesActivity::class.java))
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                }
                R.id.nav_storage_analyzer -> {
                    startActivity(Intent(this, StorageAnalyzerActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupRecyclerView() {
        fileAdapter = FileAdapter(
            onItemClick = { fileItem ->
                handleFileClick(fileItem)
            },
            onItemLongClick = { fileItem ->
                showFileOptionsDialog(fileItem)
                true
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadFiles(currentPath)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showStoragePermissionDialog()
            } else {
                loadFiles(currentPath)
            }
        } else {
            val permissions = mutableListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                loadFiles(currentPath)
            }
        }
    }

    private fun showStoragePermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app requires full storage access to manage files. Please grant the permission in settings.")
            .setPositiveButton("Grant") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Denied")
            .setMessage("Storage permission is required to browse files. Please grant the permission.")
            .setPositiveButton("Retry") { _, _ ->
                checkPermissions()
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .show()
    }

    private fun loadFiles(directory: File) {
        currentPath = directory
        updateToolbarTitle()
        viewModel.loadFiles(directory, preferenceManager.showHiddenFiles())
    }

    private fun navigateToPath(path: File) {
        if (path.exists() && path.isDirectory) {
            loadFiles(path)
        } else {
            Toast.makeText(this, "Directory not accessible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFileClick(fileItem: FileItem) {
        if (fileItem.isDirectory) {
            loadFiles(fileItem.file)
        } else {
            openFile(fileItem)
        }
    }

    private fun openFile(fileItem: FileItem) {
        when (fileItem.mimeType) {
            "image" -> {
                val intent = Intent(this, ImageViewerActivity::class.java)
                intent.putExtra("file_path", fileItem.path)
                startActivity(intent)
            }
            "video" -> {
                val intent = Intent(this, VideoPlayerActivity::class.java)
                intent.putExtra("file_path", fileItem.path)
                startActivity(intent)
            }
            "text", "code" -> {
                val intent = Intent(this, TextEditorActivity::class.java)
                intent.putExtra("file_path", fileItem.path)
                startActivity(intent)
            }
            "pdf" -> {
                val intent = Intent(this, PdfViewerActivity::class.java)
                intent.putExtra("file_path", fileItem.path)
                startActivity(intent)
            }
            else -> {
                openFileWithDefaultApp(fileItem.file)
            }
        }

        viewModel.addToRecent(fileItem)
    }

    private fun openFileWithDefaultApp(file: File) {
        // Implementation for opening with default app
    }

    private fun showFileOptionsDialog(fileItem: FileItem) {
        val options = listOf(
            "Open", "Share", "Copy", "Move", "Rename", "Delete",
            "Properties", "Add to Favorites", "Compress"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(fileItem.name)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> handleFileClick(fileItem)
                    1 -> shareFile(fileItem)
                    2 -> copyFile(fileItem)
                    3 -> moveFile(fileItem)
                    4 -> renameFile(fileItem)
                    5 -> deleteFile(fileItem)
                    6 -> showProperties(fileItem)
                    7 -> addToFavorites(fileItem)
                    8 -> compressFile(fileItem)
                }
            }
            .show()
    }

    private fun showCreateDialog() {
        val options = arrayOf("New Folder", "New File")
        MaterialAlertDialogBuilder(this)
            .setTitle("Create")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCreateFolderDialog()
                    1 -> showCreateFileDialog()
                }
            }
            .show()
    }

    private fun shareFile(fileItem: FileItem) {
        // Share implementation
    }

    private fun copyFile(fileItem: FileItem) {
        // Copy implementation
    }

    private fun moveFile(fileItem: FileItem) {
        // Move implementation
    }

    private fun renameFile(fileItem: FileItem) {
        // Rename implementation
    }

    private fun deleteFile(fileItem: FileItem) {
        // Delete implementation
    }

    private fun showProperties(fileItem: FileItem) {
        // Properties dialog
    }

    private fun addToFavorites(fileItem: FileItem) {
        viewModel.addToFavorites(fileItem)
        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
    }

    private fun compressFile(fileItem: FileItem) {
        // Compress implementation
    }

    private fun showCreateFolderDialog() {
        // Create folder dialog
    }

    private fun showCreateFileDialog() {
        // Create file dialog
    }

    private fun updateToolbarTitle() {
        supportActionBar?.title = currentPath.name.ifEmpty { "Storage" }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Search implementation
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_view_mode -> {
                toggleViewMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortDialog() {
        // Sort dialog implementation
    }

    private fun toggleViewMode() {
        // Toggle view mode implementation
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            currentPath != Environment.getExternalStorageDirectory() -> {
                currentPath.parentFile?.let { loadFiles(it) }
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}