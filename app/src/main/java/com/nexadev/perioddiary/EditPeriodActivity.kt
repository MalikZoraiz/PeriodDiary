package com.nexadev.perioddiary

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nexadev.perioddiary.data.database.AppDatabase
import com.nexadev.perioddiary.data.database.PeriodEntry
import com.nexadev.perioddiary.databinding.ActivityEditPeriodBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class EditPeriodActivity : BaseCalendarActivity() {

    private lateinit var binding: ActivityEditPeriodBinding
    private val selectedDates = mutableListOf<Calendar>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPeriodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            // 1. Correctly get all period entries
            val periodEntries = AppDatabase.getDatabase(applicationContext).periodEntryDao().getAllPeriodEntries()

            // 2. Convert to a list of Calendar objects (works with the new PeriodEntry class)
            val allDates = periodEntries
                .filter { it.type == "PERIOD_DAY" } // We only edit period days
                .map { it.date.toCalendar() }

            selectedDates.addAll(allDates)
            setupEditCalendar()
        }

        binding.closeButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { saveAndFinish() }
    }

    private fun setupEditCalendar() {
        setupCalendar(
            recyclerView = binding.editPeriodCalendarRecyclerView,
            selectedDates = selectedDates,
            selectionEnabled = true,
            showPredictions = false,
            isHorizontal = false,
            startYear = 2024,
            startMonth = Calendar.JANUARY
        ) { date ->
            val isSelected = selectedDates.any { it.isSameDay(date) }

            if (isSelected) {
                val datesToRemove = mutableListOf<Calendar>()
                val processingQueue = mutableListOf(date)
                val processed = mutableSetOf<Calendar>()

                while (processingQueue.isNotEmpty()) {
                    val currentDay = processingQueue.removeAt(0)
                    if (selectedDates.any { it.isSameDay(currentDay) } && !processed.any { it.isSameDay(currentDay) }) {
                        datesToRemove.add(currentDay)
                        processed.add(currentDay)

                        val dayBefore = currentDay.clone() as Calendar
                        dayBefore.add(Calendar.DAY_OF_MONTH, -1)
                        processingQueue.add(dayBefore)

                        val dayAfter = currentDay.clone() as Calendar
                        dayAfter.add(Calendar.DAY_OF_MONTH, 1)
                        processingQueue.add(dayAfter)
                    }
                }
                selectedDates.removeAll { toRemove -> datesToRemove.any { it.isSameDay(toRemove) } }

            } else {
                val today = Calendar.getInstance()
                if (date.after(today)) return@setupCalendar

                for (i in 0 until 5) {
                    val dayToAdd = date.clone() as Calendar
                    dayToAdd.add(Calendar.DAY_OF_MONTH, i)
                    if (dayToAdd.after(today)) continue
                    if (!selectedDates.any { it.isSameDay(dayToAdd) }) {
                        selectedDates.add(dayToAdd)
                    }
                }
            }
            binding.editPeriodCalendarRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun saveAndFinish() {
        lifecycleScope.launch {
            val periodEntryDao = AppDatabase.getDatabase(applicationContext).periodEntryDao()

            // 1. Delete all old local period entries
            periodEntryDao.deleteAllPeriodEntries()

            // 2. Create new entries from the selected dates
            val newPeriodEntries = selectedDates.map {
                PeriodEntry(date = it.time, type = "PERIOD_DAY")
            }
            periodEntryDao.insertAll(newPeriodEntries)

            // 3. Sync changes to Firebase if user is logged in
            val auth = Firebase.auth
            val user = auth.currentUser
            if (user != null) {
                val db = Firebase.firestore
                val userId = user.uid
                val collectionRef = db.collection("users").document(userId).collection("period_entries")

                try {
                    // First, delete all existing entries in Firestore for this user
                    val existingEntries = collectionRef.get().await()
                    val deleteBatch = db.batch()
                    for (document in existingEntries) {
                        deleteBatch.delete(document.reference)
                    }
                    deleteBatch.commit().await()
                    Log.d("EditPeriodActivity", "Successfully deleted old entries from Firestore.")

                    // Now, upload the new set of entries
                    val uploadBatch = db.batch()
                    newPeriodEntries.forEach { entry ->
                        val docRef = collectionRef.document(entry.date.time.toString())
                        uploadBatch.set(docRef, entry)
                    }
                    uploadBatch.commit().await()
                    Log.d("EditPeriodActivity", "Successfully uploaded new entries to Firestore.")

                } catch (e: Exception) {
                    Log.w("EditPeriodActivity", "Error syncing edited period data to Firestore", e)
                    Toast.makeText(applicationContext, "Cloud sync failed. Your data is saved locally.", Toast.LENGTH_LONG).show()
                }
            }

            setResult(RESULT_OK) // Set the result to indicate data has changed
            finish()
        }
    }

    private fun Calendar.isSameDay(other: Calendar): Boolean {
        return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
               this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }

    private fun Date.toCalendar(): Calendar {
        return Calendar.getInstance().apply { time = this@toCalendar }
    }
}