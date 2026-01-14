package com.nexadev.perioddiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.databinding.ActivityDeleteDataBinding
import kotlinx.coroutines.launch

class DeleteDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowDeleteData.setOnClickListener { finish() }

        binding.deleteDataButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Warning!")
            .setMessage("All the saved data will be deleted")
            .setPositiveButton("Delete") { _, _ ->
                deleteAllData()
            }
            .setNegativeButton("Cancel", null) // This dismisses the dialog, returning to the page
            .show()
    }

    private fun deleteAllData() {
        lifecycleScope.launch {
            // Clear Room Database
            val database = AppDatabase.getDatabase(applicationContext)
            database.userDao().deleteAllUsers()
            database.periodEntryDao().deleteAllPeriodEntries()

            // Clear SharedPreferences flag
            val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("onboarding_complete", false)
                apply()
            }

            // Navigate to MainActivity
            val intent = Intent(this@DeleteDataActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Finish this activity so the user can't navigate back to it
        }
    }
}