package com.nexadev.perioddiary

import android.content.Context
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
import com.nexadev.perioddiary.databinding.ActivityLogMorePeriodsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class LogMorePeriodsActivity : BaseCalendarActivity() {
    private lateinit var binding: ActivityLogMorePeriodsBinding
    private val selectedDates = mutableListOf<Calendar>()
    private val newPeriodsToSave = mutableListOf<PeriodEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogMorePeriodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowLogMore.setOnClickListener { finish() }
        binding.confirmButtonLogMore.isEnabled = false

        intent.getLongArrayExtra("selectedDates")?.forEach { millis ->
            selectedDates.add(Calendar.getInstance().apply { timeInMillis = millis })
        }

        setupCalendar(
            recyclerView = binding.monthsRecyclerViewLogMore,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = true, // We can show predictions here
            isHorizontal = false
        ) { date ->
            val today = Calendar.getInstance()

            if (date.after(today)) return@setupCalendar

            val startOfPeriod = date.clone() as Calendar
            val newPeriodDates = mutableListOf<Calendar>()

            repeat(5) {
                val dayToAdd = startOfPeriod.clone() as Calendar
                if (dayToAdd.after(today)) return@repeat

                val isAlreadySelected = selectedDates.any {it.isSameDay(dayToAdd) }

                if (!isAlreadySelected) {
                    newPeriodDates.add(dayToAdd)
                }
                startOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }

            if (newPeriodDates.isNotEmpty()) {
                selectedDates.addAll(newPeriodDates)

                val entriesToAdd = newPeriodDates.map {
                    PeriodEntry(date = it.time, type = "PERIOD_DAY")
                }
                newPeriodsToSave.addAll(entriesToAdd)
                
                binding.monthsRecyclerViewLogMore.adapter?.notifyDataSetChanged()
                binding.confirmButtonLogMore.isEnabled = true
            }
        }

        binding.confirmButtonLogMore.setOnClickListener {
            saveAndFinish()
        }
    }

    private fun saveAndFinish() {
        lifecycleScope.launch {
            if (newPeriodsToSave.isNotEmpty()) {
                // Always save to local database
                val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()
                periodEntryDao.insertAll(newPeriodsToSave)

                // If user is logged in, also sync to Firebase
                val auth = Firebase.auth
                val user = auth.currentUser
                if (user != null) {
                    val db = Firebase.firestore
                    val userId = user.uid
                    val batch = db.batch()

                    newPeriodsToSave.forEach { entry ->
                        val docRef = db.collection("users").document(userId).collection("period_entries").document(entry.date.time.toString())
                        batch.set(docRef, entry)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("LogMorePeriodsActivity", "New period data successfully synced to Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Log.w("LogMorePeriodsActivity", "Error syncing new period data to Firestore", e)
                            Toast.makeText(applicationContext, "Cloud sync failed. Your data is saved locally.", Toast.LENGTH_LONG).show()
                        }
                }
            }

            // Mark onboarding as complete so the app starts on the dashboard next time
            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("onboarding_complete", true)
                apply()
            }

            // Navigate to the dashboard to show the final result
            startActivity(Intent(this@LogMorePeriodsActivity, DashboardActivity::class.java))
            finishAffinity() // Clear the onboarding stack
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}