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
import com.nexadev.perioddiary.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.loginCloseButton.setOnClickListener { finish() }
        binding.loginCancelButton.setOnClickListener { finish() }

        binding.signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.loginDoneButton.setOnClickListener {
            val email = binding.loginEmailEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, now download data
                        Log.d("LoginActivity", "Login successful.")
                        downloadPeriodDataFromFirestore()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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

        val db = Firebase.firestore
        val userId = user.uid

        db.collection("users").document(userId).collection("period_entries")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("LoginActivity", "No period data found in Firestore for this user.")
                    goToDashboard()
                    return@addOnSuccessListener
                }

                val periodEntries = documents.toObjects(PeriodEntry::class.java)

                lifecycleScope.launch {
                    // Save downloaded data to local Room database
                    val localDb = AppDatabase.getDatabase(applicationContext)
                    localDb.periodEntryDao().deleteAllPeriodEntries() // Clear old local data first
                    localDb.periodEntryDao().insertAll(periodEntries)
                    Log.d("LoginActivity", "Data successfully downloaded from Firestore and saved to Room.")
                    goToDashboard()
                }
            }
            .addOnFailureListener { e ->
                Log.w("LoginActivity", "Error downloading data from Firestore", e)
                Toast.makeText(this@LoginActivity, "Error restoring data: ${e.message}", Toast.LENGTH_LONG).show()
                // Still go to dashboard, but data is not restored
                goToDashboard()
            }
    }

    private fun goToDashboard() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}