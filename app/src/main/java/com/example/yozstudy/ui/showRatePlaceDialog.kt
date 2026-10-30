package com.example.yozstudy.ui

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.yozstudy.R
import com.example.yozstudy.databinding.DialogRatePlaceBinding

fun Activity.showRatePlaceDialog(
    placeName: String,
    onReviewSaved: (rating: Float, isSilence: Boolean, isWifi: Boolean, isComfort: Boolean) -> Unit
) {
    val dialogBinding = DialogRatePlaceBinding.inflate(LayoutInflater.from(this))

    val dialog = AlertDialog.Builder(this)
        .setView(dialogBinding.root)
        .create()

    // Format the dynamic title using string resources and the place name
    dialogBinding.tvRateTitle.text = getString(R.string.dialog_rate_place_title, placeName)

    dialogBinding.btnSkip.setOnClickListener {
        dialog.dismiss()
    }

    dialogBinding.btnSaveReview.setOnClickListener {
        val rating = dialogBinding.ratingBar.rating
        val isSilence = dialogBinding.chipSilence.isChecked
        val isWifi = dialogBinding.chipWifi.isChecked
        val isComfort = dialogBinding.chipComfort.isChecked

        onReviewSaved(rating, isSilence, isWifi, isComfort)
        dialog.dismiss()
    }

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.show()
}