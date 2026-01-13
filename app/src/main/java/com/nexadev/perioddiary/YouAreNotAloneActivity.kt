package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivityYouAreNotAloneBinding

class YouAreNotAloneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYouAreNotAloneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYouAreNotAloneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowNotAlone.setOnClickListener { finish() }

        binding.nextButtonNotAlone.setOnClickListener {
            startActivity(Intent(this, FeelYourBestActivity::class.java))
        }
    }
}