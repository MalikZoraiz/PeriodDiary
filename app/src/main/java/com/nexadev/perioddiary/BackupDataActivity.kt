package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.User
import com.nexadev.perioddiary.databinding.ActivityBackupDataBinding
import kotlinx.coroutines.launch

class BackupDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackupDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowBackupData.setOnClickListener { finish() }

        lifecycleScope.launch {
            val user = AppDatabase.getDatabase(applicationContext).userDao().getUser()
            binding.autoBackupSwitch.isChecked = user?.backupEmail != null
        }

        binding.autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showConfigureBackupDialog()
            } else {
                // Handle turning off auto backup if needed
            }
        }
    }

    private fun showConfigureBackupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_configure_backup, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.backup_email_edit_text)
        val passwordInput = dialogView.findViewById<EditText>(R.id.backup_password_edit_text)
        val loginText = dialogView.findViewById<TextView>(R.id.already_have_account_text)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.done_button).setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                    val user = userDao.getUser()
                    val updatedUser = user?.copy(backupEmail = email) ?: User(name = "", backupEmail = email)
                    userDao.insertUser(updatedUser)
                    
                    runOnUiThread {
                        Toast.makeText(this@BackupDataActivity, "Backup created successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            } else {
                Toast.makeText(this@BackupDataActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
            binding.autoBackupSwitch.isChecked = false // Revert the switch state
        }

        loginText.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        dialog.setOnCancelListener {
            binding.autoBackupSwitch.isChecked = false // Revert switch if dialog is dismissed
        }

        dialog.show()
    }
}