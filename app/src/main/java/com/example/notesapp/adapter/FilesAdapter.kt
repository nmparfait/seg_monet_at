package com.example.notesapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.FileItemBinding
import com.example.notesapp.utils.showToast
import java.io.File

class FilesAdapter(private val list: ArrayList<File>, private val openFile: OpenFile) :
    RecyclerView.Adapter<FilesAdapter.FilesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesHolder {
        return FilesHolder(
            FileItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilesHolder, position: Int) {
        val model = list[position]
        with(holder.binding) {
            tvTitle.text = model.name
            cRoot.setOnClickListener {
                openFile.openItem(model)
            }
        }

    }

    override fun getItemCount() = list.size

    class FilesHolder(val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface OpenFile {
        fun openItem(file: File)
    }
}