package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivitySymptomsBinding

class SymptomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomsBinding
    private val selectedSymptoms = mutableSetOf<String>() // Using Set to avoid duplicates automatically

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowSymptoms.setOnClickListener { finish() }
        binding.confirmButtonSymptoms.isEnabled = false

        val symptomViews: Map<View, String> = mapOf(
            binding.symptomCramps to "Abdominal cramps",
            binding.symptomHeadache to "Headache",
            binding.symptomFatigue to "Fatigue",
            binding.symptomCravings to "Cravings",
            binding.symptomBloating to "Bloating",
            binding.symptomNoSymptoms to "No symptoms"
        )

        symptomViews.forEach { (view, name) ->
            view.setOnClickListener {
                handleSymptomSelection(view, name, symptomViews)
            }
        }

        // KTX/Kotlin: Simplified navigation using scope functions
        binding.skipButtonSymptoms.setOnClickListener { navigateNext() }
        binding.confirmButtonSymptoms.setOnClickListener { navigateNext() }
    }

    private fun handleSymptomSelection(selectedView: View, selectedName: String, allViews: Map<View, String>) {
        val noSymptomsKey = "No symptoms"

        if (selectedName == noSymptomsKey) {
            // Logic: If user clicks "No Symptoms", clear everything else
            allViews.keys.forEach { it.isSelected = false }
            selectedSymptoms.clear()

            selectedView.isSelected = true
            selectedSymptoms.add(selectedName)
        } else {
            // Logic: If user clicks a specific symptom, unselect "No Symptoms"
            allViews.filter { it.value == noSymptomsKey }.keys.forEach {
                it.isSelected = false
            }
            selectedSymptoms.remove(noSymptomsKey)

            // Toggle selection state
            selectedView.isSelected = !selectedView.isSelected
            if (selectedView.isSelected) {
                selectedSymptoms.add(selectedName)
            } else {
                selectedSymptoms.remove(selectedName)
            }
        }

        // KTX Optimization: Button is enabled if list isn't empty
        binding.confirmButtonSymptoms.isEnabled = selectedSymptoms.isNotEmpty()
    }

    private fun navigateNext() {
        // You could save selectedSymptoms to Database/SharedPreferences here
        startActivity(Intent(this, YouAreNotAloneActivity::class.java))
        finish()
    }
}