package com.example.proyecto_final_car_wash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityDetallePedido : AppCompatActivity() {
    private lateinit var confirmacionTextView: TextView;
    private lateinit var servicioTextView: TextView;
    private lateinit var vehiculoTextView: TextView;
    private lateinit var lavadoTextView: TextView;
    private lateinit var fechaTextView: TextView;
    private lateinit var totalTextView: TextView;

    private lateinit var verUbicacionSeleccionadaButton: Button;

    private lateinit var regresarButton: ImageButton;

    private lateinit var imageView: ImageView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_pedido)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        confirmacionTextView = findViewById(R.id.confirmacionTextView);
        servicioTextView = findViewById(R.id.servicioTextView);
        vehiculoTextView = findViewById(R.id.vehiculoTextView);
        lavadoTextView = findViewById(R.id.lavadoTextView);
        fechaTextView = findViewById(R.id.fechaTextView);
        totalTextView = findViewById(R.id.totalTextView);

        verUbicacionSeleccionadaButton = findViewById(R.id.verUbicacionSeleccionadaButton);

        regresarButton = findViewById(R.id.regresarButton);

        imageView = findViewById(R.id.imageView);

        val vehiculo = intent.getStringExtra("vehiculo");
        val fecha = intent.getStringExtra("fecha");
        val lavado = intent.getStringExtra("lavado");
        val servicio = intent.getStringExtra("servicio");
        val total = intent.getStringExtra("total");
        val confirmacion = intent.getBooleanExtra("confirmacion", false);
        val numeroPedido = intent.getIntExtra("numeroPedido", -1);

        val latitud = intent.getDoubleExtra("latitud", 0.0);
        val longitud = intent.getDoubleExtra("longitud", 0.0);

        if (servicio == "Domicilio") {
            verUbicacionSeleccionadaButton.visibility = View.VISIBLE;

            verUbicacionSeleccionadaButton.setOnClickListener {
                // Redirigimos a ActivityVerUbicacionPedido.kt.
                val intent = Intent(this, ActivityVerUbicacionPedido::class.java);

                intent.putExtra("latitud", latitud);
                intent.putExtra("longitud", longitud);

                startActivity(intent);
            }
        } else {
            verUbicacionSeleccionadaButton.visibility = View.GONE;
        }


        regresarButton.setOnClickListener {
            // Redirigimos a ActivityHistorial.kt.
            val intent = Intent(this, ActivityHistorial::class.java);
            startActivity(intent);
        }

        if (numeroPedido != -1) {
            fetchPedidosDetalles(
                vehiculo, fecha, lavado, servicio, total, confirmacion
            );
        } else {
            Toast.makeText(this, "Numero de pedido invalido.", Toast.LENGTH_SHORT).show();
        }

        confirmacionTextView.text =
            if (confirmacion) "Su pedido fue realizado con exito." else "Su pedido esta siendo lavado."

        if (confirmacion) {
            confirmacionTextView.setBackgroundColor(Color.parseColor("#ddf2ef"))
            confirmacionTextView.setTextColor(Color.parseColor("#005e03"))
        } else {
            confirmacionTextView.setBackgroundColor(Color.parseColor("#ffe793"))
            confirmacionTextView.setTextColor(Color.parseColor("#5a514a"))
        }
    }

    private fun fetchPedidosDetalles(
        vehiculo: String?,
        fecha: String?,
        lavado: String?,
        servicio: String?,
        total: String?,
        confirmacion: Boolean,
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid;

        if (userId != null && vehiculo != null) {
            val db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user =
                        document.toObject(com.example.proyecto_final_car_wash.User::class.java);

                    val pedidos = document.get("pedidos") as? List<Map<String, Any>> ?: emptyList();

                    val pedido = pedidos.find {
                        it["vehiculo"]?.toString() == vehiculo || it["fecha"]?.toString() == fecha || it["lavado"]?.toString() == lavado || it["servicio"]?.toString() == servicio || it["total"]?.toString() == total || it["confirmacion"]?.toString()
                            ?.toBoolean() == confirmacion
                    }

                    if (pedido != null) {
                        vehiculoTextView.text = pedido["vehiculo"] as? String;
                        fechaTextView.text = pedido["fecha"] as? String;
                        lavadoTextView.text = pedido["lavado"] as? String;
                        servicioTextView.text = pedido["servicio"] as? String;
                        totalTextView.text = "Lps. ${(pedido["total"] as? Long)?.toString()}.00";
                        confirmacionTextView.text =
                            if (pedido["confirmacion"] as? Boolean == true) "Su pedido fue realizado con exito." else "Su pedido esta siendo lavado.";

                        val imageUrl = when (pedido["vehiculo"].toString()) {
                            "Turismo" -> R.drawable.turismo
                            "4x4" -> R.drawable.a4x4
                            "Camion" -> R.drawable.camion
                            "Bus" -> R.drawable.bus
                            "Trailer" -> R.drawable.trailer
                            else -> R.drawable.logo
                        }
                        imageView.setImageResource(imageUrl)
                    } else {
                        Toast.makeText(this, "Pedido no encontrado.", Toast.LENGTH_SHORT).show();
                    }
                }
            }.addOnFailureListener { error ->
                Log.e("Error", error.toString());
            }
        }
    }
}