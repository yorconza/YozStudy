package com.example.yozstudy.ui

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.yozstudy.databinding.DialogPickTimeBinding

fun Activity.showPickTimeDialog(initialHour: Int, initialMinute: Int, onTimeSelected: (Int, Int) -> Unit) {
    val dialogBinding = DialogPickTimeBinding.inflate(LayoutInflater.from(this))

    val dialog = AlertDialog.Builder(this)
        .setView(dialogBinding.root)
        .create()

    // Display initial hours and minutes formatted to 2 digits
    dialogBinding.tvHours.text = String.format("%02d", initialHour)
    dialogBinding.tvMinutes.text = String.format("%02d", initialMinute)

    dialogBinding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }

    dialogBinding.btnAccept.setOnClickListener {
        onTimeSelected(initialHour, initialMinute)
        dialog.dismiss()
    }

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}