package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.databinding.ActivityBackupDataBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class BackupDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowBackupData.setOnClickListener { finish() }

        // Check if a user is already logged into Firebase
        val currentUser = Firebase.auth.currentUser
        binding.autoBackupSwitch.isChecked = currentUser != null

        binding.autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If switch is turned on, go to the SignUpActivity
                startActivity(Intent(this, SignUpActivity::class.java))
            } else {
                // Handle turning off auto backup - Log out the user
                Firebase.auth.signOut()
                // We can also add logic here to ask the user for confirmation
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the switch state when returning to the activity
        val currentUser = Firebase.auth.currentUser
        binding.autoBackupSwitch.isChecked = currentUser != null
    }
}