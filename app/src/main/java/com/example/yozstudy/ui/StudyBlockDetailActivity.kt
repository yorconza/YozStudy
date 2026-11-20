package com.example.yozstudy.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yozstudy.R
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.viewmodel.ServiceLocator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyBlockDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var ivBlockImage: ImageView
    private lateinit var tvSubjectTitle: TextView
    private lateinit var tvTopicDescription: TextView
    private lateinit var tvDateValue: TextView
    private lateinit var tvDurationValue: TextView
    private lateinit var tvPriorityValue: TextView
    private lateinit var tvStatusValue: TextView
    private lateinit var tvPlaceValue: TextView

    private lateinit var btnMarkAsDone: Button
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var currentBlockId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_study_block_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the ID passed via Intent
        currentBlockId = intent.getLongExtra("EXTRA_BLOCK_ID", -1L)

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (currentBlockId != -1L) {
            loadBlockDetails(currentBlockId)
        } else {
            Toast.makeText(this, "Error: Invalid block ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        ivBlockImage = findViewById(R.id.ivBlockPhoto)
        tvSubjectTitle = findViewById(R.id.tvSubject)
        tvTopicDescription = findViewById(R.id.tvTopic)
        tvDateValue = findViewById(R.id.tvDateTime)
        tvDurationValue = findViewById(R.id.tvDuration)
        tvPriorityValue = findViewById(R.id.tvPriorityBadge)
        tvStatusValue = findViewById(R.id.tvStatus)
        tvPlaceValue = findViewById(R.id.tvPlace)

        btnMarkAsDone = findViewById(R.id.btnToggleStatus)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        // 1. MARK AS DONE / PENDING
        btnMarkAsDone.setOnClickListener {
            val repository = ServiceLocator.repository
            val block = repository.getBlockSync(currentBlockId) ?: return@setOnClickListener

            val newStatus = if (block.status == BlockStatus.DONE) {
                BlockStatus.PENDING
            } else {
                BlockStatus.DONE
            }

            val updatedBlock = block.copy(status = newStatus)
            repository.updateBlockSync(updatedBlock)

            loadBlockDetails(currentBlockId)
            val msg = if (newStatus == BlockStatus.DONE) "Block marked as done!" else "Block marked as pending"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 2. EDIT (Opens the form with the loaded ID)
        btnEdit.setOnClickListener {
            val intent = Intent(this, StudyBlocksFormActivity::class.java).apply {
                putExtra("EXTRA_BLOCK_ID", currentBlockId)
            }
            startActivity(intent)
        }

        // 3. DELETE (With confirmation dialog)
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Block")
                .setMessage("Are you sure you want to delete this study block?")
                .setPositiveButton("Yes") { _, _ ->
                    val repository = ServiceLocator.repository
                    repository.deleteBlockSync(blockId = currentBlockId)
                    Toast.makeText(this, "Block deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadBlockDetails(blockId: Long) {
        val repository = ServiceLocator.repository
        val block = repository.getBlockSync(blockId)

        if (block == null) {
            Toast.makeText(this, "Block not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Subject
        val subject: Subject? = repository.getSubjectsSync().find { it.id == block.subjectId }
        tvSubjectTitle.text = subject?.name ?: "No subject"

        // Topic
        tvTopicDescription.text = block.topic

        // Date & Time (e.g., Fri 16/10 · 14:00)
        val dateFormat = SimpleDateFormat("E dd/MM · HH:mm", Locale.getDefault())
        tvDateValue.text = dateFormat.format(Date(block.startAtMillis))

        // Duration
        tvDurationValue.text = "${block.durationMinutes} min"

        // Priority
        tvPriorityValue.text = block.priority.name.lowercase().replaceFirstChar { it.uppercase() }

        // Status
        val isDone = block.status == BlockStatus.DONE
        tvStatusValue.text = if (isDone) "Done" else "Pending"
        btnMarkAsDone.text = if (isDone) "Mark as pending" else "Mark as done"

        // Study Place
        val placeName = block.placeId?.let { placeId ->
            repository.getPlaceSync(placeId)?.name
        } ?: "No place assigned"
        tvPlaceValue.text = placeName
    }
}