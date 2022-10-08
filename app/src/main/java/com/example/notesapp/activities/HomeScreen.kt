package com.example.notesapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.notesapp.R
import com.example.notesapp.billing.BillingClass
import com.example.notesapp.databinding.ActivityHomeScreenBinding
import com.example.notesapp.utils.openActivity
import com.example.notesapp.utils.showBanner
import com.example.notesapp.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeScreen : AppCompatActivity() {

    private val binding by lazy {
        ActivityHomeScreenBinding.inflate(layoutInflater)
    }

    private var firebaseAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private lateinit var loja : BillingClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        mDatabase = Firebase.database.getReference("Notes")
        binding.toolbar.menuBtn.setImageResource(R.drawable.ic_back_new)
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.toolbar.mainTitleTxtView.text = getString(R.string.home)

        binding.toolbar.menuBtn.setOnClickListener {
            onBackPressed()
        }

        binding.btnNotes.setOnClickListener {
            openActivity(MainActivity::class.java)
        }

        binding.btnSaved.setOnClickListener {
            openActivity(FilesScreen::class.java)
        }

        showBanner(binding.bannerAd)
        checkPremium()
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