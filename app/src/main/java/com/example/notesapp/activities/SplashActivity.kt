package com.example.notesapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.notesapp.databinding.ActivitySplashBinding
import com.example.notesapp.utils.checkPermission
import com.example.notesapp.utils.isNetworkConnected
import com.example.notesapp.utils.openActivity
import com.example.notesapp.utils.showToast

class SplashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bStarted.setOnClickListener {
            checkPermission(onGranted = {
                if (isNetworkConnected()) {
                    openActivity(LoginActivity::class.java)
                } else {
                    showToast("No Internet Available")
                }

            })
        }


    }


}
