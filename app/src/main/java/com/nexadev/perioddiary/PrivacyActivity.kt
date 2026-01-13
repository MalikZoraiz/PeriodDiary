package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getStartedButtonPrivacy.isEnabled = false

        val checkboxListener = { _: Any, _: Any ->
            binding.getStartedButtonPrivacy.isEnabled = binding.checkboxProcessData.isChecked && binding.checkboxPrivacyTerms.isChecked
        }

        binding.checkboxProcessData.setOnCheckedChangeListener(checkboxListener)
        binding.checkboxPrivacyTerms.setOnCheckedChangeListener(checkboxListener)

        binding.getStartedButtonPrivacy.setOnClickListener {
            val intent = Intent(this, NameActivity::class.java)
            startActivity(intent)
        }

        binding.backArrowPrivacy.setOnClickListener {
            finish()
        }
    }
}