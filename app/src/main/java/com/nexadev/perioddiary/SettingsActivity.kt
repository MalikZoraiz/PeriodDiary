package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.User
import com.nexadev.perioddiary.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowSettings.setOnClickListener { finish() }

        lifecycleScope.launch {
            val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
            binding.userNameText.text = user?.name ?: ""
        }

        binding.yourNameRow.setOnClickListener {
            showEditNameDialog()
        }

        binding.backupDataLabel.setOnClickListener {
            startActivity(Intent(this, BackupDataActivity::class.java))
        }

        binding.deleteDataLabel.setOnClickListener {
            startActivity(Intent(this, DeleteDataActivity::class.java))
        }
    }

    private fun showEditNameDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_name, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_name_input)

        lifecycleScope.launch {
            val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
            nameInput.setText(user?.name ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle("Change Your Name")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch {
                        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                        val user = userDao.getUser()
                        if (user != null) {
                            userDao.insertUser(user.copy(name = newName))
                            binding.userNameText.text = newName
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}