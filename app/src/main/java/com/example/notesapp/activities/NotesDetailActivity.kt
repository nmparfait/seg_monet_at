package com.example.notesapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.notesapp.R
import com.example.notesapp.databinding.ActivityNotesDetailBinding
import com.example.notesapp.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class NotesDetailActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityNotesDetailBinding.inflate(layoutInflater)
    }

    private var progressDialog: ProgressDialog? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        handleClicks()

    }

    private fun init() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait")
        progressDialog?.setMessage("Loading...")

        intent?.let {
            binding.toolbar.mainTitleTxtView.text = it.getStringExtra("TITLE")
            binding.etTitle.text = it.getStringExtra("TITLE")
            binding.etDescription.text = it.getStringExtra("DETAIL")
            val imgUri = it.getStringExtra("IMG")
            Glide.with(this).load(imgUri?.toUri()).into(binding.ivProfile)
        }
        binding.toolbar.menuBtn.setImageResource(R.drawable.ic_back_new)
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.etDescription.movementMethod = ScrollingMovementMethod()

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()
    }

    private fun handleClicks() {
        binding.toolbar.menuBtn.setOnClickListener {
            onBackPressed()
        }
    }

}