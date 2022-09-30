package com.example.project

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityInsertMovieDataBinding
import com.example.project.model.MovieDetailModelClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Suppress("DEPRECATION")
class InsertMovieDataActivity : AppCompatActivity() {

    private val binding: ActivityInsertMovieDataBinding by lazy {
        ActivityInsertMovieDataBinding.inflate(layoutInflater)
    }
    var progressDialog: ProgressDialog? = null

    private var db: FirebaseFirestore? = null
    private var imgUri: Uri? = null
    private var storageReference: StorageReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    private var mGetContent =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imgUri = uri
                Glide.with(this).load(uri).into(binding.imageView)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please Wait...")
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        with(binding) {
            btnInsertData.setOnClickListener {
                if (imgUri==null) {
                    Toast.makeText(
                        this@InsertMovieDataActivity,
                        "Please Select image",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (etName.text!!.isNotEmpty() && etEpisode.text!!.isNotEmpty() && etNotes.text!!.isNotEmpty())
                    addDataToFirestore(
                        etName.text.toString(),
                        etEpisode.text.toString(),
                        etNotes.text.toString()
                    )
                else
                    Toast.makeText(
                        this@InsertMovieDataActivity,
                        "Please insert all data",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            imageView.setOnClickListener {
                mGetContent.launch("image/*")
            }
        }
    }

    private fun addDataToFirestore(
        name: String,
        movieEpisode: String,
        description: String
    ) {
        progressDialog?.show()
        storageReference?.child("profilePics/" + firebaseAuth?.currentUser?.uid + "/${System.currentTimeMillis()}")
            ?.putFile(imgUri!!)?.addOnCompleteListener {
                it.result.storage.downloadUrl.addOnCompleteListener { uri ->
                    val dbCourses = db!!.collection("Movie")
                    val courses = MovieDetailModelClass(
                        index = "0",
                        movieDescription = description,
                        movieName = name,
                        movieEpisode = movieEpisode,
                        moviePoster = uri.result.toString()
                    )

                    dbCourses.add(courses).addOnSuccessListener {
                        progressDialog?.dismiss()
                        Toast.makeText(
                            this,
                            "Your Details has been added to Firebase Firestore",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e ->
                        progressDialog?.dismiss()
                        Toast.makeText(
                            this,
                            "Fail to add course \n$e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}