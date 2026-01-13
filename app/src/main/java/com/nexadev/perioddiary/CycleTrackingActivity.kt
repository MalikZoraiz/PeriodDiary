package com.nexadev.perioddiary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nexadev.perioddiary.databinding.ActivityCycleTrackingBinding

class CycleTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCycleTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCycleTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}