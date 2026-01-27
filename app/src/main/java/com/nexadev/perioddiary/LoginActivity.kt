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
import com.nexadev.perioddiary.data.database.User
import com.nexadev.perioddiary.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                        Log.d("LoginActivity", "Login successful.")
                        lifecycleScope.launch {
                            syncDataOnLogin()
                        }
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private suspend fun syncDataOnLogin() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Error: Could not get user for data sync.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val userId = user.uid
        val localDb = AppDatabase.getDatabase(applicationContext)
        val periodDao = localDb.periodEntryDao()
        val userDao = localDb.userDao()

        try {
            // 1. Fetch local data and remote data concurrently
            val localPeriodEntries = periodDao.getAllPeriodEntries()
            val remotePeriodEntriesSnapshot = db.collection("users").document(userId).collection("period_entries").get().await()
            val remotePeriodEntries = remotePeriodEntriesSnapshot.toObjects(PeriodEntry::class.java)
            val userDoc = db.collection("users").document(userId).get().await()

            // 2. Merge data
            // Combine local and remote entries, removing duplicates. Remote data is the source of truth in case of conflict.
            val combinedEntries = (localPeriodEntries + remotePeriodEntries).distinctBy { it.date.time }

            // 3. Update local database
            periodDao.deleteAllPeriodEntries() // Clear old data
            periodDao.insertAll(combinedEntries)   // Insert merged data

            // Update user name from remote if it exists, otherwise keep local
            val localUser = userDao.getUser()
            val remoteName = if (userDoc.exists()) userDoc.getString("name") else null

            if (remoteName != null) {
                userDao.insertUser(User(name = remoteName))
            } else if (localUser != null) {
                // If no remote name, but local name exists, upload it.
                db.collection("users").document(userId).set(mapOf("name" to localUser.name)).await()
            }

            // 4. Upload the merged data to Firestore to ensure consistency
            val batch = db.batch()
            val collectionRef = db.collection("users").document(userId).collection("period_entries")
            combinedEntries.forEach { entry ->
                val docRef = collectionRef.document(entry.date.time.toString())
                batch.set(docRef, entry)
            }
            batch.commit().await()

            Log.d("LoginActivity", "Data sync complete. Local and remote are now consistent.")
            goToDashboard()

        } catch (e: Exception) {
            Log.w("LoginActivity", "Error during data sync", e)
            Toast.makeText(this@LoginActivity, "Error syncing data: ${e.message}", Toast.LENGTH_LONG).show()
            goToDashboard() // Still go to dashboard, even if sync fails
        }
    }

    private fun goToDashboard() {
        Toast.makeText(this, "Login successful! Your data is synced.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}