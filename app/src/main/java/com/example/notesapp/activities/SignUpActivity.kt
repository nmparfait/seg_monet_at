package com.example.notesapp.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.notesapp.BuildConfig
import com.example.notesapp.R
import com.example.notesapp.databinding.ActivitySignUpBinding
import com.example.notesapp.model.NotesModel
import com.example.notesapp.utils.TakePictureWithUriReturnContract
import com.example.notesapp.utils.checkCamera
import com.example.notesapp.utils.isLocationEnabled
import com.example.notesapp.utils.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class SignUpActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var imgUri: Uri? = null
    private var progressDialog: ProgressDialog? = null
    private var mDatabase: DatabaseReference? = null
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var etEmail = ""
    private var etPassword = ""
    private var etName = ""
    private var etDate = ""
    private var currentLocation = ""

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
        getLocation()
    }

    private fun init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        firebaseAuth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance()
        mDatabase = Firebase.database.getReference("Notes")
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait")
        progressDialog?.setMessage("Loading...")
        binding.toolbar.menuBtn.visibility = View.GONE
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.toolbar.mainTitleTxtView.text = getString(R.string.sign_up)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient?.lastLocation?.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val list: List<Address> =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    Log.d(
                        "asdasdas",
                        "getLocation: ${list[0].subLocality},${list[0].locality},${list[0].countryName}"
                    )

                    currentLocation =
                        "${list[0].subLocality}, ${list[0].locality}, \n${list[0].countryName}"
                }
            }?.addOnFailureListener {
                showToast(it.message.toString())
            }
        } else {
            showToast("Please Enable Location")
        }

    }

    private fun handleClicks() {
        binding.btSignUp.setOnClickListener {
            if (isLocationEnabled()) {
                etEmail = binding.etEmail.text.toString()
                etPassword = binding.etPassword.text.toString()
                etName = binding.etName.text.toString()
                etDate = binding.etDate.text.toString()
                if (imgUri == null) {
                    Toast.makeText(this, "Please select profile image", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (etEmail.isNotEmpty() && etPassword.isNotEmpty() && etName.isNotEmpty() && etDate.isNotEmpty()) {
                    if (isEmailValid(etEmail)) {
                        createNewAccount(etEmail, etPassword)
                    } else {
                        Toast.makeText(this, "Your Email Id is Invalid.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show()
                }
            } else {
                showToast("Please Enable Location")
            }

        }

        binding.ibDate.setOnClickListener {
            datePickerDialog()
        }

        binding.ivProfile.setOnClickListener {
            //mGetContent.launch("image/*")
            checkCamera({
                takeImage()
            })

        }
    }

    private fun createNewAccount(email: String, password: String) {
        progressDialog?.show()
        firebaseAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    imgUri?.let {
                        val uploadTask =
                            storageReference?.child("profilePics/" + firebaseAuth?.currentUser?.uid)
                                ?.putFile(it)
                        uploadTask?.addOnCompleteListener { task1: Task<UploadTask.TaskSnapshot?> ->
                            if (task1.isSuccessful) {
                                val userId = firebaseAuth?.uid ?: return@addOnCompleteListener
                                writeData(
                                    userId,
                                    etName,
                                    etEmail,
                                    etDate,
                                    currentLocation = currentLocation
                                )
                            } else {
                                progressDialog?.dismiss()
                                Log.d("fException", "createNewAccount: ${task.exception?.message}")
                                Toast.makeText(this, task1.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                } else {
                    progressDialog?.dismiss()
                    Log.d("fException", "createNewAccount: ${task.exception?.message}")
                    Toast.makeText(this, "" + task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }?.addOnFailureListener { e: Exception ->
                Log.d("fException", "createNewAccount: ${e.message}")
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            }
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun datePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select Date")
            .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT).build()
        datePicker.addOnPositiveButtonClickListener {
            val date = sdf.format(it)
            binding.etDate.setText(date)
        }
        datePicker.show(supportFragmentManager, "TAG")


    }

    private fun writeData(
        userId: String,
        name: String,
        email: String,
        date: String,
        premiumAccess: Boolean = false,
        currentLocation: String
    ) {
        val notesModel = NotesModel(name, email, date, premiumAccess, currentLocation)
        mDatabase?.child(userId)?.setValue(notesModel)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                progressDialog?.dismiss()
                Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                progressDialog?.dismiss()
                Log.d("fException", "createNewAccount: ${task.exception?.message}")
                showToast("task.exception?.message")
            }

        }?.addOnFailureListener {
            progressDialog?.dismiss()
            Log.d("addOnFailureListener", "createNewAccount: ${it.message}")
            showToast(it.message.toString())
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

}