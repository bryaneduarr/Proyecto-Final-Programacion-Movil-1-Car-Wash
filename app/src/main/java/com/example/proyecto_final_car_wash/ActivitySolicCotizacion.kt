package com.example.proyecto_final_car_wash

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivitySolicCotizacionBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitySolicCotizacion : AppCompatActivity() {
    // Asignamos el binding.
    private lateinit var binding: ActivitySolicCotizacionBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Asignando variables.
        binding = ActivitySolicCotizacionBinding.inflate(layoutInflater);
        setContentView(binding.root);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Asignamos la lista de cada seleccion.
        val vehiculos = arrayOf("Turismo", "4x4", "Camion", "Bus", "Trailer");
        val lavados = arrayOf("Lavado general", "Lavado interior y exterior", "Lavado de motor");
        val servicios = arrayOf("Domicilio", "Instalaciones");

        // Creamos los adapters para cada seleccion
        val vehiculosAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, vehiculos);
        val lavadosAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, lavados);
        val serviciosAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, servicios);

        // Asignamos los adapters a cada uno.
        binding.vehiculoAutoCompleteTextView.setAdapter(vehiculosAdapter);
        binding.lavadoAutoCompleteTextView.setAdapter(lavadosAdapter);
        binding.servicioAutoCompleteTextView.setAdapter(serviciosAdapter);

        // Manejamos los clicks para los menu.
        binding.vehiculoAutoCompleteTextView.setOnClickListener {
            binding.vehiculoAutoCompleteTextView.showDropDown()
        }
        binding.lavadoAutoCompleteTextView.setOnClickListener {
            binding.lavadoAutoCompleteTextView.showDropDown()
        }
        binding.servicioAutoCompleteTextView.setOnClickListener {
            binding.servicioAutoCompleteTextView.showDropDown()
        }

        // Manejamos el click de la fecha.
        binding.fechaTextInputEditText.setOnClickListener {
            datePicker(binding.fechaTextInputEditText);
        }
    }

    // Funcion para mostrar el calendario.
    private fun datePicker(editText: TextInputEditText) {
        // Construimos el calendario de Material.
        val datePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText("Selecciona una fecha").build();

        // Manejamos si salio bien al dar click
        datePicker.addOnPositiveButtonClickListener { seleccion ->
            // Asignamos el tipo de fecha que queremos.
            val simpleDateFormate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            val fecha = simpleDateFormate.format(Date(seleccion));

            // Y aqui lo configuramos para ponerlo en el edit text.
            editText.setText(fecha);
        }

        // Mostramos el datePicker.
        datePicker.show(supportFragmentManager, "datePicker");
    }
}