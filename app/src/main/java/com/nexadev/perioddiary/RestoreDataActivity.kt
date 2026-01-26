package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityRestoreDataBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestoreDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestoreDataBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestoreDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.backArrowRestoreData.setOnClickListener { finish() }

        binding.restoreDataButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("RestoreDataActivity", "signInWithEmail:success")
                        downloadPeriodDataFromFirestore()
                    } else {
                        Log.w("RestoreDataActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun downloadPeriodDataFromFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Could not get user for data sync.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val db = Firebase.firestore
                val userId = user.uid

                val periodEntriesSnapshot = db.collection("users").document(userId).collection("period_entries").get().await()
                val periodEntries = periodEntriesSnapshot.toObjects(PeriodEntry::class.java)

                // Save to local database
                val localDb = AppDatabase.getDatabase(applicationContext)
                localDb.periodEntryDao().deleteAllPeriodEntries()
                localDb.periodEntryDao().insertAll(periodEntries)

                Log.d("RestoreDataActivity", "Data successfully downloaded and saved to Room.")
                goToDashboard()

            } catch (e: Exception) {
                Log.w("RestoreDataActivity", "Error downloading data from Firestore", e)
                Toast.makeText(this@RestoreDataActivity, "Error restoring data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun goToDashboard() {
        Toast.makeText(this, "Data restored successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}