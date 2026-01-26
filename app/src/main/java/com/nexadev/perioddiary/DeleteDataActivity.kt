package com.nexadev.perioddiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.databinding.ActivityDeleteDataBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DeleteDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowDeleteData.setOnClickListener { finish() }

        binding.deleteDataButton.setOnClickListener {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(applicationContext)
                database.periodEntryDao().deleteAllPeriodEntries()

                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putBoolean("onboarding_complete", false)
                    apply()
                }

                Toast.makeText(this@DeleteDataActivity, "Local data has been deleted.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@DeleteDataActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
            }
        }

        binding.deleteAccountPermanentlyButton.setOnClickListener {
            showReauthenticationDialog()
        }
    }

    private fun showReauthenticationDialog() {
        val auth = Firebase.auth
        val user = auth.currentUser

        if (user == null || user.email == null) {
            Toast.makeText(this, "You must be logged in to delete an account.", Toast.LENGTH_LONG).show()
            return
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reauthenticate, null)
        val passwordEditText = dialogView.findViewById<TextInputEditText>(R.id.password_edit_text_reauth)

        AlertDialog.Builder(this)
            .setTitle("Confirm Account Deletion")
            .setView(dialogView)
            .setPositiveButton("Delete Permanently") { _, _ ->
                val password = passwordEditText.text.toString()
                if (password.isNotEmpty()) {
                    lifecycleScope.launch {
                        reauthenticateAndDelete(user.email!!, password)
                    }
                } else {
                    Toast.makeText(this, "Password is required.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun reauthenticateAndDelete(email: String, password: String) {
        val auth = Firebase.auth
        val user = auth.currentUser

        if (user == null) { // Should not happen if we got this far
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        try {
            user.reauthenticate(credential).await()
            Log.d("DeleteDataActivity", "User re-authenticated successfully.")

            // --- Deletion Starts Here ---

            // 1. Delete Firestore data
            val db = Firebase.firestore
            val userId = user.uid
            val periodEntriesQuery = db.collection("users").document(userId).collection("period_entries").get()
            val periodEntriesSnapshot = periodEntriesQuery.await()

            val batch = db.batch()
            for (document in periodEntriesSnapshot) {
                batch.delete(document.reference)
            }
            batch.commit().await()
            Log.d("DeleteDataActivity", "Firestore data deleted successfully.")

            // 2. Delete local data
            val database = AppDatabase.getDatabase(applicationContext)
            database.periodEntryDao().deleteAllPeriodEntries()
            database.userDao().deleteAllUsers()
            Log.d("DeleteDataActivity", "Local database cleared.")

            // 3. Delete Firebase Auth user
            user.delete().await()
            Log.d("DeleteDataActivity", "Firebase user account deleted successfully.")

            // 4. Reset the onboarding flow and go to main activity
            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("onboarding_complete", false)
                apply()
            }

            Toast.makeText(this@DeleteDataActivity, "Your account and all data have been permanently deleted.", Toast.LENGTH_LONG).show()
            val intent = Intent(this@DeleteDataActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()

        } catch (e: Exception) {
            Log.w("DeleteDataActivity", "Error during re-authentication or deletion", e)
            Toast.makeText(this, "Deletion failed. Please check your password and try again.", Toast.LENGTH_LONG).show()
        }
    }
}