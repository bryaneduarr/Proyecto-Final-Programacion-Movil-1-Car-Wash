package com.example.proyecto_final_car_wash

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivityNavigationBinding
import com.google.android.material.tabs.TabLayoutMediator

class ActivityNavigation : AppCompatActivity() {
    // Declaramos la variable binding
    // para traer los datos del xml
    // facilmente.
    private lateinit var binding: ActivityNavigationBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityNavigationBinding.inflate(layoutInflater);
        setContentView(binding.root);

        // Obtenemos los valores del xml
        val viewPager = binding.viewPager;
        val tabLayout = binding.tabLayout;

        // Declaramos el ViewPager que creamos.
        viewPager.adapter = ViewPager(this);

        // Aqui es donde le decimos la pocision
        // de los tabs y el nombre
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                // Le decimos los nombres
                0 -> "Mi Perfil";
                1 -> "Seguridad";

                // Si hay un tab que no existe
                // entonces sera null.
                else -> null;
            }
            // Lo atamos y que se vean los cambios.
        }.attach();


    }
}