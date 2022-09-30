package com.example.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.databinding.AdapterItemBinding
import com.example.project.model.MovieDetailModelClass

class ItemAdapter(
    private val mainList: ArrayList<MovieDetailModelClass>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<ItemAdapter.NotesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesHolder {
        val binding = AdapterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        val item = mainList[position]
        with(holder.binding) {
            tvTitle.text=item.movieName
            tvMovieEpisode.text=item.movieEpisode
            Glide.with(tvTitle.context).load(item.moviePoster).into(ivImage)
            cRoot.setOnClickListener {
                itemClickListener.itemClick(item)
            }
        }
    }

    override fun getItemCount() = mainList.size

    class NotesHolder(val binding: AdapterItemBinding) : RecyclerView.ViewHolder(binding.root)

    interface ItemClickListener {
        fun itemClick(model: MovieDetailModelClass)
    }
}