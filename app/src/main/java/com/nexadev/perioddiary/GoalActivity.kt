package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.nexadev.perioddiary.databinding.ActivityGoalBinding
import com.nexadev.perioddiary.R
import android.widget.RadioButton

class GoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalBinding
    private var selectedGoalCardId: Int = -1

    // Extension function to convert DP to PX for stroke width
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.confirmButtonGoal.isEnabled = false
        binding.backArrowGoal.setOnClickListener { finish() }

        setupGoalSelection()

        binding.confirmButtonGoal.setOnClickListener {
            if (selectedGoalCardId == R.id.card_cycle_tracking) {
                startActivity(Intent(this, LastPeriodActivity::class.java))
            }
        }
    }

    private fun setupGoalSelection() {
        val goals = mapOf(
            binding.goalCycleTracking.root to (binding.goalCycleTracking.cardCycleTracking to binding.goalCycleTracking.radioCycleTracking),
            binding.goalTryingToConceive.root to (binding.goalTryingToConceive.cardTryingToConceive to binding.goalTryingToConceive.radioTryingToConceive),
            binding.goalPregnancyTracking.root to (binding.goalPregnancyTracking.cardPregnancyTracking to binding.goalPregnancyTracking.radioPregnancyTracking),
            binding.goalPerimenopause.root to (binding.goalPerimenopause.cardPerimenopause to binding.goalPerimenopause.radioPerimenopause)
        )

        fun selectOption(selectedCard: MaterialCardView, selectedRb: RadioButton) {
            // Reset all other cards and radio buttons
            goals.values.forEach { (card, rb) ->
                card.strokeWidth = 1.dpToPx()
                rb.isChecked = false
            }

            // Highlight the selected card and check its radio button
            selectedCard.strokeWidth = 2.dpToPx()
            selectedRb.isChecked = true
            selectedGoalCardId = selectedCard.id

            // Enable confirm button only if "Cycle Tracking" is the selected goal
            binding.confirmButtonGoal.isEnabled = selectedGoalCardId == R.id.card_cycle_tracking
        }

        goals.forEach { (container, pair) ->
            container.setOnClickListener {
                selectOption(pair.first, pair.second)
            }
        }
    }
}