package com.filemanager.pro.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.filemanager.pro.databinding.ActivityPdfViewerBinding
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("file_path") ?: run {
            finish()
            return
        }

        val pdfFile = File(filePath)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI(pdfFile)
    }

    private fun setupUI(pdfFile: File) {
        setSupportActionBar(binding.toolbarPdf)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = pdfFile.name

        binding.toolbarPdf.setNavigationOnClickListener {
            finish()
        }

        // Load PDF
        binding.pdfView.fromFile(pdfFile)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(10)
            .onLoad { nbPages ->
                binding.textPageInfo.text = "Total pages: $nbPages"
            }
            .onPageChange { page, pageCount ->
                binding.textPageInfo.text = "Page ${page + 1} / $pageCount"
            }
            .onError { throwable ->
                Toast.makeText(this, "Error loading PDF: ${throwable.message}",
                    Toast.LENGTH_SHORT).show()
            }
            .load()
    }
}