package com.example.project

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private var firebaseAuth: FirebaseAuth? = null
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        with(binding) {
            btSignUp.setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        SignupActivity::class.java
                    )
                )
            }
            btLogin.setOnClickListener { v ->
                val etEmail = binding.etEmail.text.toString()
                val etPassword = binding.etPassword.text.toString()
                if (!etEmail.isEmpty() && !etPassword.isEmpty()) {
                    if (isEmailValid(etEmail)) {
                        signInUser(etEmail, etPassword)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Your Email Id is Invalid.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Fill required fields", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please Wait...")
    }

    private fun signInUser(email: String, password: String) {
        progressDialog!!.show()
        firebaseAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    progressDialog!!.dismiss()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
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
            startActivity(Intent(this, MainActivity::class.java))
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