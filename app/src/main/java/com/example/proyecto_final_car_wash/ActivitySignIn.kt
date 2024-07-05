package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivitySignInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ActivitySignIn : AppCompatActivity() {
    // Declaramos el binding para acceder
    // al xml facilmente.
    private lateinit var binding: ActivitySignInBinding;

    // Declaramos el auth de Firebase.
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivitySignInBinding.inflate(layoutInflater);
        setContentView(binding.root);

        // Inicializar el auth de firebase.
        auth = Firebase.auth;

        binding.signInButton.setOnClickListener {
            // Validamos los camopos.
            if (validateField()) {
                // Declaramos las variables para correo y contraseña.
                val email = binding.emailEditText.text.toString();
                val password = binding.passwordEditText.text.toString();

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Declaramos una variable para saber el usuario actual.
                        val usuario = auth.currentUser;

                        // Verificamos que el usuario no sea nulo.
                        if (usuario !== null) {
                            // Declaramos una variable para saber si el usuario esta validado.
                            val verificacion = usuario.isEmailVerified;

                            // Verificamos si el usuario valido su correo.
                            if (verificacion) {
                                // Mensaje de Inicio de sesion correcto.
                                Toast.makeText(
                                    this, "Inicio de sesion correcto!", Toast.LENGTH_SHORT
                                ).show();

                                // Llevar al MainActivity.
                                val intent = Intent(this, MainActivity::class.java);
                                startActivity(intent);

                                // Finalizar el ActivitySignIn
                                finish();
                            } else {
                                // Mostar un mensaje de error si al iniciar sesion paso un error.
                                Toast.makeText(
                                    this, "Porfavor verifique su correo.", Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                    } else {
                        // Si hubo algun error entonces mostrar
                        // un mensaje toast.
                        Toast.makeText(this, "Hubo algo mal al iniciar sesion.", Toast.LENGTH_SHORT)
                            .show();
                    }
                }
            }
        }

        // Cuando le demos click al text view
        // de No tengo una cuenta haremos lo
        // siguiente.
        binding.notRegisteredTextView.setOnClickListener {
            // Enviamos al usuario al ActivitySignUp.kt.
            val intent = Intent(this, ActivitySignUp::class.java);
            startActivity(intent);

            // Finalizamos el ActivitySignIn.kt.
            finish();
        }


        // Cuando le demos click al text view
        // de Olvide mi contraseña haremos lo
        // siguiente.
        binding.forgotPasswordTextView.setOnClickListener {
            // Enviamos al usuario al ActivityForgotPassword.kt.
            val intent = Intent(this, ActivityForgotPassword::class.java);
            startActivity(intent);

            // Finalizamos el ActivitySignIn.kt.
            finish();
        }
    }

    // Funcion para validar los campos
    // no esten vacios.
    private fun validateField(): Boolean {
        // Incializamos las variables a string.
        val email = binding.emailEditText.text.toString().trim();
        val password = binding.passwordEditText.text.toString().trim();

        return when {
            email.isEmpty() -> {
                // Mensaje de error.
                binding.emailEditText.error = "El campo de email está vacío";

                // Nos pondra el recuadro en un color
                // para que el usuario vea donde esta
                // el error.
                binding.emailEditText.requestFocus();

                // Si el campo email/correo esta vacio
                // retornar falso.
                false;
            }

            password.isEmpty() -> {
                // Mensaje de error.
                binding.passwordEditText.error = "El campo de contraseña está vacío";

                // Nos pondra el recuadro en un color
                // para que el usuario vea donde esta
                // el error.
                binding.passwordEditText.requestFocus();

                // Si el campo contraseña esta vacio
                // retornar falso.
                false;
            }

            // Si los campos estan llenos
            // retornar true.
            else -> true;
        }
    }

    // Funcion para que siempre que se inicie el activity
    // revise si el usuario ya estaba registrado antes.
    override fun onStart() {
        super.onStart();

        // Declaramos una variable para el usuario actual.
        val usuario = auth.currentUser;

        // Revisamos si el usuario estaba
        // registrado.
        if (usuario != null) {
            // Verificamos si el usuario esta verificado.
            if (usuario.isEmailVerified) {
                // Si esta registrado entonces llevarlo al
                // MainActivity.kt.
                val intent = Intent(this, MainActivity::class.java);
                startActivity(intent);
            }
        }
    }
}