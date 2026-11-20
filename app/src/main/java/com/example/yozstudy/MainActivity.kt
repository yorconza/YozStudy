package com.example.yozstudy

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import com.example.yozstudy.ui.StudyBlockDetailActivity
import com.example.yozstudy.ui.StudyBlocksFormActivity
import com.example.yozstudy.ui.StudyPlacesActivity
import com.example.yozstudy.ui.adapter.StudyBlockAdapter
import com.example.yozstudy.util.util
import com.example.yozstudy.viewmodel.ServiceLocator.repository
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var blockAdapter: StudyBlockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Standard margin adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvMainTitle)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<LinearLayout>(R.id.bottomNavigation)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom
            v.layoutParams = params
            insets
        }

        // 1. Floating button (+) to open the block form
        val btnOpenForm = findViewById<FloatingActionButton>(R.id.btnOpenForm)
        btnOpenForm.setOnClickListener {
            util.openActivity(this, StudyBlocksFormActivity::class.java)
        }

        // 2. "Map" option in the bottom navigation bar
        val btnOpenPlaces = findViewById<TextView>(R.id.btnOpenPlaces)
        btnOpenPlaces.setOnClickListener {
            util.openActivity(this, StudyPlacesActivity::class.java)
        }

        // 3. Initialize RecyclerView
        setupMainActivityRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        // Carga y calcula estadísticas al iniciar o volver de otra pantalla
        loadRoutineBlocks()
    }

    private fun setupMainActivityRecyclerView() {
        blockAdapter = StudyBlockAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, StudyBlockDetailActivity::class.java).apply {
                putExtra("EXTRA_BLOCK_ID", selectedItem.block.id)
            }
            startActivity(intent)
        }

        val rvStudyBlocks = findViewById<RecyclerView>(R.id.rvStudyBlocks)
        rvStudyBlocks.layoutManager = LinearLayoutManager(this)
        rvStudyBlocks.adapter = blockAdapter
    }

    private fun loadRoutineBlocks() {
        val blocks = repository.getBlocksSync()
        val subjects = repository.getSubjectsSync()
        val places = repository.getPlacesSync()

        // Cambia 'const' por 'val' aquí:
        val detailsList = blocks.map { block ->
            StudyBlockWithDetails(
                block = block,
                subject = subjects.firstOrNull { it.id == block.subjectId },
                place = block.placeId?.let { placeId -> places.firstOrNull { it.id == placeId } }
            )
        }

        blockAdapter.updateData(detailsList)
    }

    private fun updateProgressStats(total: Int, completed: Int) {
        val tvProgressCount = findViewById<TextView>(R.id.tvProgressCount)
        val pbDailyProgress = findViewById<ProgressBar>(R.id.pbDailyProgress)

        tvProgressCount?.text = "$completed de $total bloques"
        pbDailyProgress?.max = if (total > 0) total else 1
        pbDailyProgress?.progress = completed
    }
}