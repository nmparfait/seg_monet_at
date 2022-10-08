package com.example.notesapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.R
import com.example.notesapp.databinding.ActivityLoginBinding
import com.example.notesapp.utils.openActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private var firebaseAuth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        handleClicks()
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog?.setTitle("Please Wait")
        progressDialog?.setMessage("Loading...")
        binding.toolbar.menuBtn.visibility = View.GONE
        binding.toolbar.statusGroup.visibility = View.GONE
        binding.toolbar.mainTitleTxtView.text = getString(R.string.login)
    }

    private fun handleClicks() {
        binding.btSignUp.setOnClickListener { v ->
            openActivity(SignUpActivity::class.java)

        }

        binding.btLogin.setOnClickListener { v ->
            val etEmail = binding.etEmail.text.toString()
            val etPassword = binding.etPassword.text.toString()
            if (etEmail.isNotEmpty() && etPassword.isNotEmpty()) {
                if (isEmailValid(etEmail)) {
                    signInUser(etEmail, etPassword)
                } else {
                    Toast.makeText(this, "Your Email Id is Invalid.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show()
            }
        }

        /*binding.tvForgotText.setOnClickListener { v ->
            openActivity(ForgotPasswordActivity::class.java)

        }*/
    }

    private fun signInUser(email: String, password: String) {
        progressDialog!!.show()
        firebaseAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    progressDialog!!.dismiss()
                    startActivity(Intent(this@LoginActivity, HomeScreen::class.java))
                    finish()
                } else {
                    progressDialog!!.dismiss()
                    Toast.makeText(this, "" + task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e: Exception ->
                progressDialog!!.dismiss()
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkCurrentUser() {
        val user = firebaseAuth!!.currentUser
        if (user != null) {
            startActivity(Intent(this, HomeScreen::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        checkCurrentUser()
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}