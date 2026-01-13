package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowSignIn.setOnClickListener { finish() }

        // You can add logic for the restore data button here
        // binding.restoreDataButton.setOnClickListener { ... }
    }
}