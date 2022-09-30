package com.example.project

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private var firebaseAuth: FirebaseAuth? = null
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        with(binding) {
            btSignUp.setOnClickListener { v ->
                val etEmail = binding.etEmail.text.toString()
                val etPassword = binding.etPassword.text.toString()
                if (etEmail.isNotEmpty() && etPassword.isNotEmpty()) {
                    if (isEmailValid(etEmail)) {
                        createNewAccount(etEmail, etPassword)
                    } else {
                        Toast.makeText(this@SignupActivity, "Your Email Id is Invalid.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignupActivity, "Fill required fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Creating Account...")
    }



    private fun createNewAccount(email: String, password: String) {
        progressDialog!!.show()
        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    finish()
                } else {
                    Toast.makeText(this, "" + task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
                progressDialog!!.dismiss()
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                progressDialog!!.dismiss()
            }
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}