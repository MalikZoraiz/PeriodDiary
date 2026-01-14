package com.nexadev.perioddiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isOnboardingComplete = sharedPref.getBoolean("onboarding_complete", false)

        if (isOnboardingComplete) {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getStartedButton.setOnClickListener {
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
        }

        binding.signInText.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}