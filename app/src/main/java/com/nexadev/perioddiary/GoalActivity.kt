package com.nexadev.perioddiary

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children // KTX for ViewGroups
import com.google.android.material.card.MaterialCardView
import com.nexadev.perioddiary.databinding.ActivityGoalBinding
import android.widget.RadioButton

class GoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalBinding
    private var selectedGoalCardId: Int = -1

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
            // KTX Optimization: Using forEach on map values directly
            goals.values.forEach { (card, rb) ->
                // KTX allows using resources more fluently
                card.strokeWidth = resources.getDimensionPixelSize(R.dimen.thin_stroke)
                rb.isChecked = false
            }

            selectedCard.strokeWidth = resources.getDimensionPixelSize(R.dimen.thick_stroke)
            selectedRb.isChecked = true
            selectedGoalCardId = selectedCard.id

            // Enable button logic
            binding.confirmButtonGoal.isEnabled = (selectedGoalCardId == R.id.card_cycle_tracking)
        }

        goals.forEach { (container, pair) ->
            container.setOnClickListener {
                selectOption(pair.first, pair.second)
            }
        }
    }
}