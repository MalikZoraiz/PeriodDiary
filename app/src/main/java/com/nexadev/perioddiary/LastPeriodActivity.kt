package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityLastPeriodBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class LastPeriodActivity : BaseCalendarActivity() {
    private lateinit var binding: ActivityLastPeriodBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastPeriodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowLastPeriod.setOnClickListener { finish() }
        binding.confirmButtonLastPeriod.isEnabled = false

        binding.notSureButton.setOnClickListener {
            val intent = Intent(this, SymptomsActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupCalendar(
            recyclerView = binding.monthsRecyclerView,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = false,
            isHorizontal = false
        ) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) return@setupCalendar

            selectedDates.clear()
            val startOfPeriod = date.clone() as Calendar

            repeat(5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) return@repeat

                selectedDates.add(dayToAdd)
                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            binding.monthsRecyclerView.adapter?.notifyDataSetChanged()
            binding.confirmButtonLastPeriod.isEnabled = true
        }

        binding.confirmButtonLastPeriod.setOnClickListener {
            saveAndNavigate()
        }
    }

    private fun saveAndNavigate() {
        lifecycleScope.launch {
            // Always save to local database
            val periodEntries = selectedDates.map { PeriodEntry(date = it.time, type = "PERIOD_DAY") }
            AppDatabase.getDatabase(applicationContext).periodEntryDao().insertAll(periodEntries)

            // If user is logged in, also sync to Firebase
            val auth = Firebase.auth
            val user = auth.currentUser
            if (user != null) {
                val db = Firebase.firestore
                val userId = user.uid
                val batch = db.batch()

                periodEntries.forEach { entry ->
                    val docRef = db.collection("users").document(userId).collection("period_entries").document(entry.date.time.toString())
                    batch.set(docRef, entry)
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("LastPeriodActivity", "Initial period data successfully synced to Firestore.")
                    }
                    .addOnFailureListener { e ->
                        Log.w("LastPeriodActivity", "Error syncing initial period data to Firestore", e)
                        Toast.makeText(applicationContext, "Cloud sync failed. Your data is saved locally.", Toast.LENGTH_LONG).show()
                    }
            }

            // Navigate to the next screen
            val datesInMillis = selectedDates.map { it.timeInMillis }.toLongArray()
            val intent = Intent(this@LastPeriodActivity, LogMorePeriodsActivity::class.java).apply {
                putExtra("selectedDates", datesInMillis)
            }
            startActivity(intent)
            finish()
        }
    }
}