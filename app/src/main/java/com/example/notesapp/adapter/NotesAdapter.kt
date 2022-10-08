package com.example.notesapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notesapp.databinding.NotesItemBinding
import com.example.notesapp.model.Notes

class NotesAdapter(
    private val notesList: ArrayList<Notes>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesHolder {
        val binding = NotesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        val item = notesList[position]
        with(holder.binding) {
            tvTitle.text = item.notesTitle
            tvDate.text = item.notesDate
            Glide.with(ivImage.context).load(item.notesImagePath?.toUri()).into(ivImage)

            ivImage.setOnClickListener {
                itemClickListener.itemClick(item)
            }
            ivSave.setOnClickListener {
                itemClickListener.saveItem(item)
            }
        }

    }

    override fun getItemCount() = notesList.size

    class NotesHolder(val binding: NotesItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun itemClick(notes: Notes)
        fun saveItem(notes: Notes)
    }
}