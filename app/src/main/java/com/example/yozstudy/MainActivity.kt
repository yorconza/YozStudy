package com.example.yozstudy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import com.example.yozstudy.databinding.ActivityMainBinding
import com.example.yozstudy.ui.StudyBlockDetailActivity

//YozStudy
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Botón flotante (+) para ir al formulario de crear/agregar bloque
        binding.fabAddBlock.setOnClickListener {
            val intent = Intent(this, StudyBlockDetailActivity::class.java)
            startActivity(intent)
        }

        // 2. Si quieres que los textos de la barra inferior funcionen como navegación:
        // (Nota: Si prefieres convertir esos TextView de la barra inferior en botones o elementos clickeables,
        // puedes asignarles un ID en tu XML, por ejemplo id="@+id/tvNavMapa" y id="@+id/tvNavProfile")

        // Ejemplo simulado si tuvieras los IDs en el XML de la barra:
        /*
        binding.tvNavMapa.setOnClickListener {
            val intent = Intent(this, StudyPlacesActivity::class.java)
            startActivity(intent)
        }
        */
    }
}