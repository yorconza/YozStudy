package com.example.yozstudy.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.yozstudy.R
import com.example.yozstudy.databinding.ActivityStudyBlocksFormBinding

class StudyBlockFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyBlocksFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyBlocksFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        // Temporary example to populate Spinners with data in English
        val subjects = listOf("Mobile Programming", "Mathematics", "English", "Physics")
        val subjectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, subjects)
        binding.spinnerSubject.adapter = subjectAdapter

        val durations = listOf("30 minutes", "60 minutes", "90 minutes", "120 minutes")
        val durationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durations)
        binding.spinnerDuration.adapter = durationAdapter

        val places = listOf("Central Library", "Home Desk", "University Lab", "Study Room A")
        val placeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, places)
        binding.spinnerPlace.adapter = placeAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            // Logic to save the study block
            finish()
        }
    }
}