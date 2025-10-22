package com.filemanager.pro.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.filemanager.pro.R
import com.filemanager.pro.databinding.ActivityTextEditorBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TextEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextEditorBinding
    private var currentFile: File? = null
    private var isModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("file_path")
        if (filePath != null) {
            currentFile = File(filePath)
            loadFile()
        }

        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbarEditor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = currentFile?.name ?: "New File"

        binding.toolbarEditor.setNavigationOnClickListener {
            handleBackPress()
        }

        // Text change listener
        binding.codeEditor.setText(binding.codeEditor.text.toString()) // Placeholder for editor
    }

    private fun loadFile() {
        currentFile?.let { file ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val content = file.readText()
                    withContext(Dispatchers.Main) {
                        binding.codeEditor.setText(content)
                        isModified = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TextEditorActivity,
                            "Error reading file: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveFile() {
        currentFile?.let { file ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    file.writeText(binding.codeEditor.text.toString())
                    withContext(Dispatchers.Main) {
                        isModified = false
                        Toast.makeText(
                            this@TextEditorActivity,
                            "File saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TextEditorActivity,
                            "Error saving file: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleBackPress() {
        if (isModified) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Unsaved Changes")
                .setMessage("Do you want to save changes before leaving?")
                .setPositiveButton("Save") { _, _ ->
                    saveFile()
                    finish()
                }
                .setNegativeButton("Discard") { _, _ ->
                    finish()
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveFile()
                true
            }
            R.id.action_undo -> {
                // Undo implementation
                true
            }
            R.id.action_redo -> {
                // Redo implementation
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        handleBackPress()
    }
}