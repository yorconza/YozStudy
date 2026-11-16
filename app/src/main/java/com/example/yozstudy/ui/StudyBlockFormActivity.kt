package com.example.yozstudy.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.yozstudy.R
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.Priority
import com.example.yozstudy.domain.model.StudyBlock
import com.example.yozstudy.domain.model.StudyPlace
import com.example.yozstudy.domain.model.Subject
import com.example.yozstudy.viewmodel.ServiceLocator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StudyBlocksFormActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etSearchBlock: EditText
    private lateinit var btnSearch: Button
    private lateinit var etSubject: EditText
    private lateinit var etTopic: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etDuration: EditText
    private lateinit var rgPriority: RadioGroup
    private lateinit var rbLow: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var rbHigh: RadioButton
    private lateinit var etPlace: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button
    private lateinit var btnSave: Button

    private var currentBlockId: Long? = null
    private val calendar: Calendar = Calendar.getInstance()
    private var selectedStartMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_study_blocks_form)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        updateDateTimeFields()

        // Check if it was opened to edit an existing block by ID
        val editingBlockId = intent.getLongExtra("EXTRA_BLOCK_ID", -1L)
        if (editingBlockId != -1L) {
            currentBlockId = editingBlockId
            loadBlockDataIntoForm(editingBlockId)
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearchBlock = findViewById(R.id.etSearchBlock)
        btnSearch = findViewById(R.id.btnSearch)
        etSubject = findViewById(R.id.etSubject)
        etTopic = findViewById(R.id.etTopic)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        etDuration = findViewById(R.id.etDuration)
        rgPriority = findViewById(R.id.rgPriority)
        rbLow = findViewById(R.id.rbLow)
        rbMedium = findViewById(R.id.rbMedium)
        rbHigh = findViewById(R.id.rbHigh)
        etPlace = findViewById(R.id.etPlace)
        btnCancel = findViewById(R.id.btnCancel)
        btnDelete = findViewById(R.id.btnDelete)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        etDate.setOnClickListener { showDatePicker() }
        etTime.setOnClickListener { showTimePicker() }

        // EXACT SEARCH BY ID
        btnSearch.setOnClickListener {
            val query = etSearchBlock.text.toString().trim()
            if (query.isNotEmpty()) {
                searchBlock(query)
            } else {
                Toast.makeText(this, "Por favor ingrese un ID o tópico para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        // CREATE OR UPDATE
        btnSave.setOnClickListener {
            if (currentBlockId == null) {
                saveNewBlock()
            } else {
                showConfirmationDialog("Actualizar Bloque", "¿Desea guardar los cambios en este bloque?") {
                    updateExistingBlock()
                }
            }
        }

        // DELETE
        btnDelete.setOnClickListener {
            if (currentBlockId != null) {
                showConfirmationDialog("Eliminar Bloque", "¿Está seguro de que desea eliminar este bloque de estudio?") {
                    deleteBlock()
                }
            } else {
                Toast.makeText(this, "Busque y cargue un bloque antes de eliminarlo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedStartMillis = calendar.timeInMillis
                updateDateTimeFields()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                selectedStartMillis = calendar.timeInMillis
                updateDateTimeFields()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeFields() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))
        etTime.setText(timeFormat.format(calendar.time))
    }

    private fun searchBlock(query: String) {
        val repository = ServiceLocator.repository
        val queryLong = query.toLongOrNull()

        val foundBlock = if (queryLong != null) {
            repository.getBlockSync(queryLong)
        } else {
            repository.getBlocksSync().find { block: StudyBlock ->
                block.topic.contains(query, ignoreCase = true)
            }
        }

        if (foundBlock != null) {
            loadBlockDataIntoForm(foundBlock.id)
            Toast.makeText(this, "Bloque cargado correctamente (ID: ${foundBlock.id})", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No se encontró ningún bloque con: $query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBlockDataIntoForm(id: Long) {
        val repository = ServiceLocator.repository
        val block = repository.getBlockSync(id) ?: return

        val subjects: List<Subject> = repository.getSubjectsSync()
        val subjectName = subjects.find { it.id == block.subjectId }?.name ?: ""

        val placeName = block.placeId?.let { placeId ->
            repository.getPlaceSync(placeId)?.name
        } ?: ""

        currentBlockId = block.id
        etSubject.setText(subjectName)
        etTopic.setText(block.topic)
        etDuration.setText(block.durationMinutes.toString())
        etPlace.setText(placeName)

        selectedStartMillis = block.startAtMillis
        calendar.timeInMillis = selectedStartMillis
        updateDateTimeFields()

        when (block.priority) {
            Priority.LOW -> rbLow.isChecked = true
            Priority.MEDIUM -> rbMedium.isChecked = true
            Priority.HIGH -> rbHigh.isChecked = true
        }

        btnSave.text = "Actualizar"
    }

    private fun saveNewBlock() {
        val subjectText = etSubject.text.toString().trim()
        val topicText = etTopic.text.toString().trim()
        val durationStr = etDuration.text.toString().trim()
        val placeText = etPlace.text.toString().trim()

        if (subjectText.isEmpty() || topicText.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val repository = ServiceLocator.repository

        // Subject Handling (Task Manager style: searches or creates on the fly)
        val existingSubject = repository.getSubjectsSync().find { it.name.equals(subjectText, ignoreCase = true) }
        val subjectId = existingSubject?.id ?: run {
            val newSubjectId = System.currentTimeMillis()
            val newSubject = Subject(newSubjectId, subjectText, android.graphics.Color.parseColor("#3F51B5"))
            repository.addSubjectSync(newSubject)
            newSubjectId
        }

        // Handling of Study Place (Task Manager style: search or create on the fly using a positional constructor)
        val selectedPlaceId = if (placeText.isNotEmpty()) {
            val existingPlace = repository.getPlacesSync().find { it.name.equals(placeText, ignoreCase = true) }
            existingPlace?.id ?: run {
                val newPlaceId = System.currentTimeMillis()
                val newPlace = StudyPlace(newPlaceId, placeText, "", 0.0, 0.0)
                repository.addPlaceSync(newPlace)
                newPlaceId
            }
        } else {
            null
        }

        val selectedPriority = when (rgPriority.checkedRadioButtonId) {
            R.id.rbLow -> Priority.LOW
            R.id.rbMedium -> Priority.MEDIUM
            else -> Priority.HIGH
        }

        val generatedId = System.currentTimeMillis()

        val newBlock = StudyBlock(
            id = generatedId,
            subjectId = subjectId,
            topic = topicText,
            startAtMillis = selectedStartMillis,
            durationMinutes = durationStr.toIntOrNull() ?: 60,
            priority = selectedPriority,
            status = BlockStatus.PENDING,
            placeId = selectedPlaceId
        )

        repository.addBlockSync(newBlock)
        Toast.makeText(this, "Bloque guardado con éxito. ID: $generatedId", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun updateExistingBlock() {
        val id = currentBlockId ?: return
        val subjectText = etSubject.text.toString().trim()
        val topicText = etTopic.text.toString().trim()
        val durationStr = etDuration.text.toString().trim()
        val placeText = etPlace.text.toString().trim()

        if (subjectText.isEmpty() || topicText.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val repository = ServiceLocator.repository
        val existingBlock = repository.getBlockSync(id)

        val existingSubject = repository.getSubjectsSync().find { it.name.equals(subjectText, ignoreCase = true) }
        val subjectId = existingSubject?.id ?: existingBlock?.subjectId ?: run {
            val newSubjectId = System.currentTimeMillis()
            val newSubject = Subject(newSubjectId, subjectText, android.graphics.Color.parseColor("#3F51B5"))
            repository.addSubjectSync(newSubject)
            newSubjectId
        }

        val selectedPlaceId = if (placeText.isNotEmpty()) {
            val existingPlace = repository.getPlacesSync().find { it.name.equals(placeText, ignoreCase = true) }
            existingPlace?.id ?: run {
                val newPlaceId = System.currentTimeMillis()
                val newPlace = StudyPlace(newPlaceId, placeText, "", 0.0, 0.0)
                repository.addPlaceSync(newPlace)
                newPlaceId
            }
        } else {
            existingBlock?.placeId
        }

        val selectedPriority = when (rgPriority.checkedRadioButtonId) {
            R.id.rbLow -> Priority.LOW
            R.id.rbMedium -> Priority.MEDIUM
            else -> Priority.HIGH
        }

        val updatedBlock = StudyBlock(
            id = id,
            subjectId = subjectId,
            topic = topicText,
            startAtMillis = selectedStartMillis,
            durationMinutes = durationStr.toIntOrNull() ?: 60,
            priority = selectedPriority,
            status = existingBlock?.status ?: BlockStatus.PENDING,
            placeId = selectedPlaceId,
            photoUri = existingBlock?.photoUri
        )

        repository.updateBlockSync(updatedBlock)
        Toast.makeText(this, "Bloque actualizado con éxito", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun deleteBlock() {
        val id = currentBlockId ?: return
        val repository = ServiceLocator.repository
        repository.deleteBlockSync(blockId = id)
        Toast.makeText(this, "Bloque eliminado", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Sí") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}