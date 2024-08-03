package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityHistorial : AppCompatActivity() {
    // Declaramos las variables inicialmente.
    private lateinit var adapter: PedidoAdapter;
    private val pedidosList = mutableListOf<Pedido>();
    private lateinit var regresarButton: ImageButton;
    private lateinit var listView: ListView;
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inicializamos el list view.;
        listView = findViewById(R.id.listView);

        // Inicializamos el boton de regreso.
        regresarButton = findViewById(R.id.regresarButton);

        // Inicializamos el adaptador que utilizaremos.
        adapter = PedidoAdapter(this, pedidosList);

        // Aqui enlzamos el list view con el adaptador.
        listView.adapter = adapter;

        // Inicializamos la autenticacion para ingresar a firebase.
        auth = FirebaseAuth.getInstance();

        // Declaramos una variable para obtener el id del usuario.
        val userId = auth.currentUser?.uid;

        // Comprobamos que el usuario exista antes de mostrar el historial.
        if (userId != null) {
            fetchPedidos(userId);
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPedido = pedidosList[position];

            val intent = Intent(this, ActivityDetallePedido::class.java).apply {
                putExtra("vehiculo", selectedPedido.vehiculo);
                putExtra("fecha", selectedPedido.fecha);
                putExtra("lavado", selectedPedido.lavado);
                putExtra("servicio", selectedPedido.servicio);
                putExtra("total", selectedPedido.total);
                putExtra("confirmacion", selectedPedido.confirmacion);
                putExtra("numeroPedido", selectedPedido.numeroPedido);
                putExtra("latitud", selectedPedido.latitud);
                putExtra("longitud", selectedPedido.longitud);
            }

            startActivity(intent);
        }

        // Manejamos el click de regresar al menu principal.
        regresarButton.setOnClickListener {
            // Redirigimos a MainActivity.kt.
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent);
        }
    }

    // Funcion para mandar a pedir el historial.
    private fun fetchPedidos(userId: String) {
        // Aqui declaramos la base de datos para poder ingresar.
        val db = FirebaseFirestore.getInstance();

        // Ingresamos a la coleccion de usuarios y buscamos el documento
        // del usuario.
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            // Comprobamos que el documento no sea nulo y exista.
            if (document != null && document.exists()) {
                // Declaramos una variable para decirle cual es la lista de pedidos.
                val pedidos =
                    document.toObject(com.example.proyecto_final_car_wash.User::class.java)?.pedidos;

                // Ejecutamos la lista de pedidos.
                pedidos?.let {
                    pedidosList.clear();
                    pedidosList.addAll(it);
                    adapter.notifyDataSetChanged();
                }
            } else {
                // Si el documento no existiera entonces entonces mostramos un error.
                Log.d("Error", "El documento no existe.");
            }
        }.addOnFailureListener { error ->
            // Mostramos un error si hubiera algun problema para conectarse a la base de datos.
            Log.d("Error", error.toString());
        }
    }
}

// Inicializamos una clase de datos para los usuarios de la base de datos.
data class User(
    val pedidos: List<Pedido> = listOf(),
);
