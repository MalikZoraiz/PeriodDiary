package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
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

        binding.firstNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.confirmButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.confirmButton.setOnClickListener {
            val name = binding.firstNameEditText.text.toString()
            if (name.isNotEmpty()) {
                lifecycleScope.launch {
                    val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                    userDao.insertUser(User(name = name))
                }
            }
            startActivity(Intent(this, GoalActivity::class.java))
        }

        binding.skipButton.setOnClickListener {
            startActivity(Intent(this, GoalActivity::class.java))
        }

        binding.backArrow.setOnClickListener {
            finish()
        }
    }
}