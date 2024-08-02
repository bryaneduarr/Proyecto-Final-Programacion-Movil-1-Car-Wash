package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivityPedidoTotalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityPedidoTotal : AppCompatActivity() {
    // Asignamos el binding.
    private lateinit var binding: ActivityPedidoTotalBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Asignando variables.
        binding = ActivityPedidoTotalBinding.inflate(layoutInflater);
        setContentView(binding.root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val vehiculo = intent.getStringExtra("vehiculo") ?: "None";
        val lavado = intent.getStringExtra("lavado") ?: "None";
        val servicio = intent.getStringExtra("servicio") ?: "None";
        val fecha = intent.getStringExtra("fecha") ?: "None";
        val precioVehiculo = intent.getDoubleExtra("precioVehiculo", 0.00);
        val precioLavado = intent.getDoubleExtra("precioLavado", 0.00);
        val precioServicio = intent.getDoubleExtra("precioServicio", 0.00);
        val total = intent.getDoubleExtra("total", 0.00);
        val latitud = intent.getDoubleExtra("latitud", 0.00)
        val longitud = intent.getDoubleExtra("longitud", 0.00)
        val subTotal = (15 / 100.0) * total;

        Log.d("Test", "Latitud: $latitud, Longitud: $longitud")

        Log.d("Precio Vehiculo: ", precioVehiculo.toString());
        Log.d("Precio Lavado: ", precioLavado.toString());
        Log.d("Precio Servicio: ", precioServicio.toString());


        binding.vehiculoPrecioTextView.text = "Lps. ${precioVehiculo}";
        binding.lavadoPrecioTextView.text = "Lps. ${precioLavado}";
        binding.servicioPrecioTextView.text = "Lps. ${precioServicio}";
        binding.impuestoPrecioTextView.text = "Lps. ${subTotal}"
        binding.totalPrecioTextView.text = "Lps. ${total + subTotal}";

        binding.regresarButton.setOnClickListener {
            // Redirigimos a ActivitySolicCotizacion.kt.
            val intent = Intent(this, ActivitySolicCotizacion::class.java);
            startActivity(intent);
        }

        binding.finalizarPedidoButton.setOnClickListener {
            enviarDatos(
                vehiculo,
                lavado,
                servicio,
                fecha,
                precioVehiculo,
                precioLavado,
                precioServicio,
                latitud,
                longitud
            );
        }
    }

    // Funcion para crear un mapa de pedidos en firebase en el usuario actual.
    private fun enviarDatos(
        vehiculo: String,
        lavado: String,
        servicio: String,
        fecha: String,
        precioVehiculo: Double,
        precioLavado: Double,
        precioServicio: Double,
        latitud: Double,
        longitud: Double,
    ) {
        // Declaramos todas las variables que vamos a mandar a firebase.
        val vehiculo = vehiculo.toString();
        val lavado = lavado.toString();
        val servicio = servicio.toString();
        val fecha = fecha.toString();
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
                        "longitud" to longitud,
                        "latitud" to latitud,
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