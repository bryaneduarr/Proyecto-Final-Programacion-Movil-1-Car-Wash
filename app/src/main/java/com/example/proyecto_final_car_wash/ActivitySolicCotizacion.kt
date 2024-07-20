package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivitySolicCotizacionBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitySolicCotizacion : AppCompatActivity() {
    // Asignamos el binding.
    private lateinit var binding: ActivitySolicCotizacionBinding;

    // Asignamos los precios para cada producto.
    private val preciosVehiculos = mapOf(
        "Turismo" to 50.00, "4x4" to 60.00, "Camion" to 70.00, "Bus" to 100.00, "Trailer" to 150.00
    );
    private val preciosLavados = mapOf(
        "Lavado general" to 100.00,
        "Lavado interior y exterior" to 150.00,
        "Lavado de motor" to 400.00
    );
    private val preciosServicios = mapOf(
        "Domicilio" to 200.00, "Instalaciones" to 160.00
    )

    // Precios por defecto.
    private var precioVehiculo = 0.00
    private var precioLavado = 0.00
    private var precioServicio = 0.00

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

        // Manejamos el click de regresar al menu principal.
        binding.regresarButton.setOnClickListener {
            // Redirigimos a MainActivity.kt.
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }

        // Manejamos el click para solicitar el pedido.
        binding.solicitarPedidoButton.setOnClickListener { enviarDatos() }

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

        // Manejamos la seleccion de cada opcion para asignar los precios.
        binding.vehiculoAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            // Declaramos el valor seleccionado por el usuario.
            val vehiculoSeleccionado = vehiculos[position];

            // Decimos que el precio del producto ahora sera
            // el del item seleccionado, si no hay nada seleccionado
            // entonces sera 0.00.
            precioVehiculo = preciosVehiculos[vehiculoSeleccionado] ?: 0.00;

            // Llamamos la funcion mostrarTotal.
            mostrarTotal();
        }
        binding.lavadoAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            // Declaramos el valor seleccionado por el usuario.
            val lavadoSeleccioando = lavados[position];

            // Decimos que el precio del producto ahora sera
            // el del item seleccionado, si no hay nada seleccionado
            // entonces sera 0.00.
            precioLavado = preciosLavados[lavadoSeleccioando] ?: 0.00;

            // Llamar la funcion para actualizar los servicios por si escogio
            // la opcion lavado de motor.
            actualizarServicios(lavadoSeleccioando);

            // Llamamos la funcion mostrarTotal.
            mostrarTotal();
        }
        binding.servicioAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            // Declaramos el valor seleccionado por el usuario.
            val servicioSeleccionado = servicios[position];

            // Decimos que el precio del producto ahora sera
            // el del item seleccionado, si no hay nada seleccionado
            // entonces sera 0.00.
            precioServicio = preciosServicios[servicioSeleccionado] ?: 0.00;

            // Llamamos la funcion mostrarTotal.
            mostrarTotal();
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

    // Funcion para manejar la opcion lavado de motor.
    private fun actualizarServicios(lavadoSeleccionado: String) {
        // Asignamos dependiendo que lavado fue seleccionado
        // el nuevo array.
        val serviciosDisponibles = if (lavadoSeleccionado == "Lavado de motor") {
            arrayOf("Instalaciones")
        } else {
            arrayOf("Domicilio", "Instalaciones");
        }

        // Declaramos el array adapter.
        val serviciosAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, serviciosDisponibles);

        // Asignamos el adapter correspondiente para mostrar los servicios.
        binding.servicioAutoCompleteTextView.setAdapter(serviciosAdapter);
        binding.servicioAutoCompleteTextView.setText("", false);
    }

    // Funcion para mostrar el total  de los productos seleccionados.
    private fun mostrarTotal() {
        // Sumamos todos los productos.
        val total = precioVehiculo + precioLavado + precioServicio;

        // Asignamos el precio al totalPagarNumbers.
        binding.totalPagarNumbers.text = String.format(Locale.getDefault(), "%.2f", total);
    }

    // Funcion para crear un mapa de pedidos en firebase en el usuario actual.
    private fun enviarDatos() {
        // Declaramos todas las variables que vamos a mandar a firebase.
        val vehiculo = binding.vehiculoAutoCompleteTextView.text.toString();
        val lavado = binding.lavadoAutoCompleteTextView.text.toString();
        val servicio = binding.servicioAutoCompleteTextView.text.toString();
        val fecha = binding.fechaTextInputEditText.text.toString();
        val total = precioVehiculo + precioLavado + precioServicio;
        val confirmacion = false;


        // Obtenemos el usuario actual.
        val user = FirebaseAuth.getInstance().currentUser;

        if (user != null) {
            // Asignamos nuestra variable de base de datos.
            val db = FirebaseFirestore.getInstance();

            // Creamos una referencia para encontrar el documento del usuario en la coleccion
            // users.
            val userRef = db.collection("users").document(user.uid);

            // Si la coleccion con el usuario existe continuamos.
            userRef.get().addOnSuccessListener { documento ->
                // Comprobamos primero que el docuemnto del usuario exista.
                if (documento.exists()) {
                    // Creamos un array list para el documento de pedidos.
                    val pedidos = documento.get("pedidos") as? ArrayList<HashMap<String, Any>>
                        ?: arrayListOf();

                    // Vamos sumando el numero de pedido para saber cuantos a tenido el usuario.
                    val numeroPedido = pedidos.size + 1;

                    // Hacemos un mapa con cada uno de los campos que mandaremos a firebase.
                    val datosPedido = hashMapOf(
                        "numeroPedido" to numeroPedido,
                        "vehiculo" to vehiculo,
                        "lavado" to lavado,
                        "servicio" to servicio,
                        "total" to total,
                        "fecha" to fecha,
                        "confirmacion" to confirmacion
                    );

                    // Aqui es donde añadimos los datos en el hashMap.
                    pedidos.add(HashMap(datosPedido));

                    // Aqui actualizamos el documento del usuario con los pedidos.
                    userRef.update("pedidos", pedidos).addOnSuccessListener {
                        // Mostramos un mensaje de confirmacion al usuario.
                        Toast.makeText(
                            this, "Pedido solicitado, espere confirmacion.", Toast.LENGTH_SHORT
                        ).show();

                        // Redirigimos a MainActivity.kt.
                        val intent = Intent(this, MainActivity::class.java);
                        startActivity(intent);
                    }.addOnFailureListener { error ->
                        // Mostramos un error si hubo algo malo al enviar el pedido.
                        Toast.makeText(
                            this, "Error al enviar el pedido.", Toast.LENGTH_SHORT
                        ).show();
                        Log.e("Error", error.toString());
                    }
                } else {
                    // Mostramos un error si el usuario no existe en la coleccion users.
                    Toast.makeText(this, "El usuario no existe.", Toast.LENGTH_SHORT).show();
                }
            }.addOnFailureListener { error ->
                // Si no encontramos al usuario  en la coleccion users mostramos un mensaje de error.
                Toast.makeText(this, "Hubo un error obteniendo el usuario", Toast.LENGTH_SHORT)
                    .show();
                Log.e("Error", error.toString());
            }
        } else {
            // Si el usuario no esta registrado entonces le decimos que inicie sesion antes de
            // continuar.
            Toast.makeText(this, "Hubo un error, porfavor inicie sesion.", Toast.LENGTH_SHORT)
                .show();
        }
    }
}