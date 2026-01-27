package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
                        var user = userDao.getUser()
                        if (user == null) {
                            user = User(name = newName)
                        }
                        userDao.insertUser(user.copy(name = newName))
                        binding.userNameText.text = newName

                        // Also save to Firestore if logged in
                        val auth = Firebase.auth
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val db = Firebase.firestore
                            val userId = currentUser.uid
                            db.collection("users").document(userId).set(mapOf("name" to newName))
                                .addOnSuccessListener { Log.d("SettingsActivity", "User name updated in Firestore.") }
                                .addOnFailureListener { e -> Log.w("SettingsActivity", "Error updating user name in Firestore", e) }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}