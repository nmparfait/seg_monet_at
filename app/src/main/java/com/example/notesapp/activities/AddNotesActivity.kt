package com.example.notesapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.notesapp.BuildConfig
import com.example.notesapp.R
import com.example.notesapp.billing.BillingClass
import com.example.notesapp.databinding.ActivityAddNotesBinding
import com.example.notesapp.model.NotesModel
import com.example.notesapp.utils.TakePictureWithUriReturnContract
import com.example.notesapp.utils.checkCamera
import com.example.notesapp.utils.showBanner
import com.example.notesapp.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddNotesActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddNotesBinding.inflate(layoutInflater)
    }

    private var firebaseAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault())
    private var progressDialog: ProgressDialog? = null
    private var imgUri: Uri? = null

    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private lateinit var loja : BillingClass

    private var title = ""
    private var description = ""

    /*private var mGetContent =
        registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imgUri = uri
                Glide.with(this).load(uri).into(binding.ivProfile)
            }
        }*/

    private val takeImageResult =
        registerForActivityResult(TakePictureWithUriReturnContract()) { (isSuccess, imageUri) ->
            if (isSuccess) {
                imgUri = imageUri
                Glide.with(this).load(imageUri).into(binding.ivProfile)
                //previewImage.setImageURI(imageUri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        handleClicks()
        showBanner(binding.bannerAd)
        checkPremium()

    }

    private fun init() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait")
        progressDialog?.setMessage("Loading...")

        binding.toolbar.menuBtn.setImageResource(R.drawable.ic_back_new)
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.toolbar.mainTitleTxtView.text = getString(R.string.add_note)
        binding.etDescription.movementMethod = ScrollingMovementMethod()

        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = Firebase.database.getReference("Notes")
    }

    private fun handleClicks() {
        binding.toolbar.menuBtn.setOnClickListener {
            onBackPressed()
        }

        binding.btnAdd.setOnClickListener {
            title = binding.etTitle.text.toString()
            description = binding.etDescription.text.toString()
            if (imgUri == null) {
                Toast.makeText(this, "Please select profile image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isNotEmpty() && description.isNotEmpty()) {
                writeNotes(title, description)
            } else {
                showToast("Fill required fields")
            }
        }

        binding.ivProfile.setOnClickListener {
            //mGetContent.launch("image/*")
            checkCamera({
                takeImage()
            })


        }
    }

    private fun writeNotes(title: String, description: String) {
        val currentDate = sdf.format(Date())
        progressDialog?.show()
        imgUri?.let { uri ->
            val uploadTask =
                storageReference?.child("profilePics/" + firebaseAuth?.currentUser?.uid + "/${System.currentTimeMillis()}")
                    ?.putFile(uri)
            uploadTask?.addOnCompleteListener { uploadTask1 ->
                if (uploadTask1.isSuccessful) {
                    uploadTask1.result.storage.downloadUrl.addOnCompleteListener { newTask ->
                        if (newTask.isSuccessful) {
                            val newUri = newTask.result
                            val userId = firebaseAuth?.uid ?: return@addOnCompleteListener
                            val notesModel = NotesModel(
                                notesTitle = title,
                                notesDetail = description,
                                notesDate = currentDate,
                                notesImagePath = newUri.toString()
                            )
                            mDatabase?.child(userId)?.child("NotesList")?.push()
                                ?.setValue(notesModel)?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        progressDialog?.dismiss()
                                        showToast("Note Added")
                                        val intent = Intent(this, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        startActivity(intent)
                                    } else {
                                        progressDialog?.dismiss()
                                        Log.d(
                                            "fException",
                                            "createNewAccount: ${task.exception?.message}"
                                        )
                                        showToast("task.exception?.message")
                                    }

                                }?.addOnFailureListener {
                                    progressDialog?.dismiss()
                                    showToast(it.message.toString())
                                }
                        } else {
                            progressDialog?.dismiss()
                            showToast(newTask.exception?.message.toString())
                        }
                    }.addOnFailureListener {
                        progressDialog?.dismiss()
                        showToast(it.message.toString())
                    }
                } else {
                    progressDialog?.dismiss()
                    showToast(uploadTask1.exception?.message.toString())
                }
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun takeImage() {
        CoroutineScope(Dispatchers.Main).launch {
            getTmpFileUri().let { uri -> takeImageResult.launch(uri) }
        }
    }

    private fun checkPremium() {
        val userId = firebaseAuth?.uid ?: return
        mDatabase?.child(userId)?.child("premiumAccess")?.setValue(true)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.bannerAd.visibility = View.GONE
                }

            }?.addOnFailureListener {
                showToast(it.message.toString())
            }
    }

    override fun onResume() {
        super.onResume()
        checkPremium()
    }
}