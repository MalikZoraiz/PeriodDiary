package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivityFeelYourBestBinding

class FeelYourBestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeelYourBestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeelYourBestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowFeelBest.setOnClickListener { finish() }

        // You can add navigation to the next screen here
        // binding.nextButtonFeelBest.setOnClickListener { ... }
    }
}