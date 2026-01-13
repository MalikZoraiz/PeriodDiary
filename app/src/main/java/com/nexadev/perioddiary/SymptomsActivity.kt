package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivitySymptomsBinding

class SymptomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySymptomsBinding
    private val selectedSymptoms = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymptomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrowSymptoms.setOnClickListener { finish() }
        binding.confirmButtonSymptoms.isEnabled = false

        // Explicitly declare the type of the map to be Map<View, String>
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

        // Navigation
        binding.skipButtonSymptoms.setOnClickListener {
            startActivity(Intent(this, YouAreNotAloneActivity::class.java))
        }

        binding.confirmButtonSymptoms.setOnClickListener {
            // You can save the selectedSymptoms list here before navigating
            startActivity(Intent(this, YouAreNotAloneActivity::class.java))
        }
    }

    private fun handleSymptomSelection(selectedView: View, selectedName: String, allViews: Map<View, String>) {
        if (selectedName == "No symptoms") {
            // If "No symptoms" is selected, deselect all others
            if (!selectedView.isSelected) {
                allViews.keys.forEach { it.isSelected = false }
                selectedSymptoms.clear()
                selectedView.isSelected = true
                selectedSymptoms.add(selectedName)
            }
        } else {
            // If any other symptom is selected, deselect "No symptoms"
            val noSymptomsView = allViews.entries.find { it.value == "No symptoms" }?.key
            if (noSymptomsView?.isSelected == true) {
                noSymptomsView.isSelected = false
                selectedSymptoms.remove("No symptoms")
            }
            // Toggle the current selection
            selectedView.isSelected = !selectedView.isSelected
            if (selectedView.isSelected) {
                if (!selectedSymptoms.contains(selectedName)) {
                    selectedSymptoms.add(selectedName)
                }
            } else {
                selectedSymptoms.remove(selectedName)
            }
        }
        binding.confirmButtonSymptoms.isEnabled = selectedSymptoms.isNotEmpty()
    }
}