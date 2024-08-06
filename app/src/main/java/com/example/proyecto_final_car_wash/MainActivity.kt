package com.example.proyecto_final_car_wash

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
    private lateinit var firestore: FirebaseFirestore
    private lateinit var welcomeTextView: TextView;

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
        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Inicializar firebase.
        auth = Firebase.auth;

        // Inicializamos firestore.
        firestore = FirebaseFirestore.getInstance();

        // Asignar variables para la
        // base de datos y el usuario
        // actual.
        val db = FirebaseFirestore.getInstance();
        val userId = auth.currentUser?.uid

        // Accion cuando demos click al circulo.
        binding.circuloButton.setOnClickListener {
            mostrarMenuPopup(it);
        }

        // Accion cuando demos click a cotizar.
        binding.cotizarButton.setOnClickListener {
            val intent = Intent(this, ActivitySolicCotizacion::class.java);
            startActivity(intent);
            finish();
        }

        // Accion cuando demos click a historial.
        binding.historialButton.setOnClickListener {
            val intent = Intent(this, ActivityHistorial::class.java);
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

                        welcomeTextView.text = "Bienvenido ${nombre}";
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

        // Ejecutamos esta operacion para buscar dentro de la coleccion
        // de usuarios y poder recibir una notificacion.
        userId?.let {
            // Declaramos la referencia para encontrar en la coleccion de usuarios
            // el id del usuario actual.
            val userRef = firestore.collection("users").document(userId);

            // Accedemos a la coleccion.
            userRef.addSnapshotListener { snapshot, error ->
                // Si hubo algun error accediendo.
                if (error != null) {
                    // Mostramos este error.
                    Log.e("Firestore", error.toString());
                }

                // Si se pudo acceder y no es nulo y existe continuamos.
                if (snapshot != null && snapshot.exists()) {
                    // Declaramos el campo de pedidos.
                    val pedidos = snapshot.get("pedidos") as? List<Map<String, Any>>;

                    // Y en los pedidos buscamos el campo de confirmacion.
                    pedidos?.forEach { pedido ->
                        // Declamaramos el campo de confirmacion como booleano.
                        val confirmacion = pedido["confirmacion"] as? Boolean;

                        // Decimos que si el campo de confirmacion es verdadero.
                        if (confirmacion == true) {
                            // Llamamos la funcion para mostrar una notificacion.
                            sendNotification()

                            // Mostramos un mensaje en la consola de exito.
                            Log.d("Notificacion", "Notificacion enviada.")
                        } else {
                            // Si el campo de confirmacion es false entonces enviamos
                            // un mensaje de error a la consola.
                            Log.e("Notificacion", "Notificacion NO enviada.")
                        }
                    }
                }
            }
        }

        // Llamamos la funcion para crear el canal de notificacion.
        createNotificationChannel();

        // Llamamos la funcion para pedir permisos de notificaciones.
        requestNotificationPermission();
    }

    // Funcion para enviar la notificacion.
    private fun sendNotification() {
        // Declaramos un id.
        val channelId = "default_channel";

        // Declaramos la funcion de la notificacion con todos los metodos.
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo).setContentTitle("Pedido verificado")
            .setContentText("Su pedido a sido verificado y estamos listos para el servicio.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
            .setContentIntent(createPendingIntent());

        // Declaramos el notificationManager.
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        // Construimos para mostrar la notificacion.
        notificationManager.notify(0, notificationBuilder.build());
    }

    // Creamos el canal de la notificacion con una funcion.
    private fun createNotificationChannel() {
        // Verificamos la version de android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Declaramos un id.
            val channelId = "default_channel";

            // Declaramos la importancia de la notificaicon.
            val importance = NotificationManager.IMPORTANCE_HIGH;

            // Declaramos el canal de notificaciones con su importancia, y sus parametros.
            val notificationChannel =
                NotificationChannel(channelId, "Default Channel", importance).apply {
                    lightColor = Color.GREEN;
                    enableVibration(true);
                };

            // Declaramos el notificationManager.
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

            // Creamos el canal de notificaciones.
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    // Funcion para decir lo que va a ser cuando demos click a la notificacion.
    private fun createPendingIntent(): PendingIntent {
        // Declaramos donde queremos ir cuando demos click a la notificacion y si
        // queremos pasar datos podemos hacerlo.
        val intent = Intent(this, ActivitySignIn::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Retornamos un valor de PendingIntent.
        return PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        );
    }

    // Funcion para declarar los permisos de notificacion.
    private fun requestNotificationPermission() {
        // Verificamos la version de android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Solicitamos los permisos de las notifcacions post.
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
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