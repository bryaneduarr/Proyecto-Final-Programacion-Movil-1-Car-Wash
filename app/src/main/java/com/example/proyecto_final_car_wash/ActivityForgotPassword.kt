package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivityForgotPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ActivityForgotPassword : AppCompatActivity() {
    // Variable para autenticacion de firebase.
    private lateinit var auth: FirebaseAuth;

    // Variable para facilitar la obtencion
    // de variables en el xml.
    private lateinit var binding: ActivityForgotPasswordBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater);
        setContentView(binding.root);

        // Inicializacion de firebase.
        auth = Firebase.auth;

        // Si damos click al text view de
        // iniciar sesion haremos lo siguiente.
        binding.iniciarSesionTextView.setOnClickListener {
            // Enviamos al usuario al ActivitySignIn.kt.
            val intent = Intent(this, ActivitySignIn::class.java);
            startActivity(intent);

            // Finalizamos el ActivityForgotPassword.kt.
            finish();
        }

        // Cuando le demos click al boton de
        // enviar mensaje para resetear la
        // contraseña haremos lo siguiente.
        binding.forgotPasswordButton.setOnClickListener {
            // Declaramos la variable de email como string.
            val email = binding.emailEditText.text.toString();

            // Revisar que el campo de correo no
            // este vacio.
            if (revisarCamposVacios()) {
                // Aqui mandamos el mensaje para resetear
                // la contraseña al correo del usuario
                // solo si existe en nuestra base de datos.
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Mostrar un mensaje de exito
                        // si salio bien.
                        Toast.makeText(this, "Correo enviado.", Toast.LENGTH_SHORT).show();

                        // Llevar al usuario al ActivitySignIn.
                        val intent = Intent(this, ActivitySignIn::class.java);
                        startActivity(intent);

                        // Finalizamos el ActivityForgotPassword.kt.
                        finish();
                    }
                }
            }
        }
    }

    // Funcion para revisar los campos vacios.
    private fun revisarCamposVacios(): Boolean {
        val email = binding.emailEditText.text.toString();

        // Si el campo de correo esta vacio entonces.
        if (binding.emailEditText.text.toString() == "") {
            // Mandar un mensaje de error.
            binding.emailLayout.error = "Este campo es reqerido.";

            // Y retornamos false.
            return false
        }

        // Si el campo esta lleno entonces
        // retornamos true.
        return true
    }
}