package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged // KTX Extension
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.User
import com.nexadev.perioddiary.databinding.ActivityNameBinding
import kotlinx.coroutines.launch

class NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmButton.isEnabled = false

        // KTX RULE: Replace 'addTextChangedListener' with 'doOnTextChanged'
        binding.firstNameEditText.doOnTextChanged { text, _, _, _ ->
            binding.confirmButton.isEnabled = !text.isNullOrEmpty()
        }

        binding.confirmButton.setOnClickListener {
            val name = binding.firstNameEditText.text.toString()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                    userDao.insertUser(User(name = name))
                }
            }
            navigateToGoal()
        }

        binding.skipButton.setOnClickListener {
            navigateToGoal()
        }

        binding.backArrow.setOnClickListener {
            finish()
        }
    }

    // Helper function to avoid code duplication
    private fun navigateToGoal() {
        val intent = Intent(this, GoalActivity::class.java)
        startActivity(intent)
    }
}