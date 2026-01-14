package com.nexadev.perioddiary

import android.content.Context
import android.content.Intent
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

        binding.nextButtonFeelBest.setOnClickListener {
            // Mark onboarding as complete
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("onboarding_complete", true)
                apply()
            }

            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}