package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    // Declarar las variables del xml.
    private lateinit var initialLetterTextView: TextView;
    private lateinit var binding: ActivityMainBinding;

    // Declarar la variable para firebase.
    private lateinit var auth: FirebaseAuth;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Asignando variables.
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        // Obterner los datos de los campos del xml
        initialLetterTextView = findViewById(R.id.initialLetterTextView);

        // Inicializar firebase.
        auth = Firebase.auth;

        // Asignar variables para la
        // base de datos y el usuario
        // actual.
        val db = FirebaseFirestore.getInstance();
        val userId = auth.currentUser?.uid

        // Accion cuando demos click al circulo.
        binding.circuloButton.setOnClickListener {
            mostrarMenuPopup(it);
        }

        binding.btnsCot.setOnClickListener {
            val intent = Intent(this, ActivitySolicCotizacion::class.java);
            startActivity(intent);
            finish();
        }

        // Revisamos que el user id no este
        // nulo para que nos lo acepte en el
        // document().
        if (userId != null) {
            // Buscar en la coleccion "users" el documento
            // relacionado con el usuario actual.
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                // Revisar si el documento si existe.
                if (document != null && document.exists()) {
                    // Buscar el documento dentro la coleccion
                    // "nombre".
                    val nombre = document.getString("nombre");

                    // Si el nombre no esta vacio entonces
                    // asignar a la variable de la letra
                    // el primer caracter como mayuscula.
                    if (!nombre.isNullOrEmpty()) {
                        val initial = nombre.first().uppercaseChar();

                        initialLetterTextView.text = initial.toString();
                    }
                }
                // Si hubo algun error entonces mostrar un
                // toast con un mensaje.
            }.addOnFailureListener {
                Toast.makeText(this, "Error al cargar los datos.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Funcion para mostrar el menu popup.
    private fun mostrarMenuPopup(view: android.view.View) {
        // Inicializamos la accion del PopupMenu.
        val popupMenu = PopupMenu(this, view);

        // Le decimos que tenemos un menu con una
        // lista.
        popupMenu.menuInflater.inflate(R.menu.menu_circle, popupMenu.menu)

        // Si le damos click a alguna opcion entonces
        // nos tendra que ejecutar lo que le digamos.
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                // Cerrar sesion.
                R.id.action_logout -> {
                    // Cerraremos sesion del usuario
                    // actual.
                    auth.signOut();

                    // Llevamos al usuario al
                    // ActivitySignIn.
                    val intent = Intent(this, ActivitySignIn::class.java);
                    startActivity(intent);

                    // Nos salimos del MainActivity.kt.
                    finish();

                    true
                }

                // Ir al perfil.
                R.id.action_profile -> {
                    // Llevamos al usuario al
                    // ActivityNavigation.
                    val intent = Intent(this, ActivityNavigation::class.java);
                    startActivity(intent);

                    // Nos salimos del MainActivity.kt.
                    finish();

                    true
                }

                else -> false
            }
        }

        // Mostramos el popupMenu.
        popupMenu.show();
    }
}