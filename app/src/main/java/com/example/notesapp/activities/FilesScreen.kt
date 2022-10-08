package com.example.notesapp.activities

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.BuildConfig
import com.example.notesapp.R
import com.example.notesapp.adapter.FilesAdapter
import com.example.notesapp.databinding.ActivityFilesScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class FilesScreen : AppCompatActivity(), FilesAdapter.OpenFile {

    private val binding by lazy {
        ActivityFilesScreenBinding.inflate(layoutInflater)
    }
    private var adapter: FilesAdapter? = null
    private var list: ArrayList<File>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        loadFiles()
    }


    private fun init() {
        list = arrayListOf()
        binding.recycle.hasFixedSize()
        binding.recycle.layoutManager = LinearLayoutManager(this)

        binding.toolbar.menuBtn.setImageResource(R.drawable.ic_back_new)
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.toolbar.mainTitleTxtView.text = getString(R.string.save_files)

        binding.toolbar.menuBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            File(filesDir.absolutePath).walk().filter { it.isFile && it.name.contains("TXT") }
                .forEach { textFile ->
                    Log.d("akljsdkljaskld", "onCreate: ${textFile.extension}")
                    list?.add(textFile)
                }
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.Main).launch {
                list?.let { files ->
                    adapter = FilesAdapter(files, this@FilesScreen)
                    binding.recycle.adapter = adapter
                }

            }
        }

    }


    private fun openFile(file: File) {
        val uriPdfPath: Uri =
            FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        Log.d("pdfPath", "" + uriPdfPath)

        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "text/plain")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this, "There is no app to load corresponding file", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun openItem(file: File) {
        openFile(file)
    }
}