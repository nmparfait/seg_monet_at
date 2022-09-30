package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.adapter.ItemAdapter
import com.example.project.databinding.ActivityMainBinding
import com.example.project.model.MovieDetailModelClass
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), ItemAdapter.ItemClickListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: ArrayList<MovieDetailModelClass>
    private var fireStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fireStore = FirebaseFirestore.getInstance();
        itemList = ArrayList()
        with(binding) {
            itemAdapter = ItemAdapter(itemList, this@MainActivity)
            rvItem.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                hasFixedSize()
                adapter = itemAdapter
            }
            btnAddMovie.setOnClickListener {
                startActivity(Intent(this@MainActivity, InsertMovieDataActivity::class.java))
            }
        }

        fireStore!!.collection("Movie").get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val list = queryDocumentSnapshots.documents
                    for (d in list) {
                        val c: MovieDetailModelClass =
                            d.toObject(MovieDetailModelClass::class.java)!!
                        itemList.add(c)
                    }
                    itemAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this,
                        "No data found in Database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fail to get the data.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun itemClick(model: MovieDetailModelClass) {
        val intent = Intent(this,ShowDataActivity::class.java)
        intent.putExtra("model",model)
        startActivity(intent)
    }
}