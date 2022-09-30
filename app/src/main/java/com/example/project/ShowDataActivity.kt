package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityShowDataBinding
import com.example.project.model.MovieDetailModelClass

class ShowDataActivity : AppCompatActivity() {

    private val binding:ActivityShowDataBinding by lazy {
        ActivityShowDataBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val list = intent.extras?.getSerializable("model") as MovieDetailModelClass
        with(binding){
            etName.setText(list.movieName)
            etEpisode.setText(list.movieEpisode)
            etNotes.setText(list.movieDescription)
            Glide.with(this@ShowDataActivity).load(list.moviePoster).into(imageView)
        }
    }
}