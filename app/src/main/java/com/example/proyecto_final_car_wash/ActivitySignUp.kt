package com.example.proyecto_final_car_wash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivitySignUp : AppCompatActivity() {
    // Declarar variables de firebase.
    private lateinit var auth: FirebaseAuth;
    private lateinit var fireStore: FirebaseFirestore;

    // Declarar variables de los campos edit text.
    private lateinit var nombreEditText: TextInputEditText;
    private lateinit var apellidoEditText: TextInputEditText;
    private lateinit var telefonoEditText: TextInputEditText;
    private lateinit var correoEditText: TextInputEditText;
    private lateinit var passwordEditText: TextInputEditText;
    private lateinit var imageViewSelection: ImageView;

    // Declarar las variables de las opciones de la foto.
    private lateinit var tomarFotoTextView: TextView;
    private lateinit var seleccionarFotoTextView: TextView;

    // Declarar la variable para traer la clase de la camera
    // photo.
    private lateinit var classesCameraPhoto: ClassesCameraPhoto;

    // Declarar la variable para traer la clase de la storage.
    private lateinit var classesStorage: ClassesStorage;

    // Declarar como vamos a tomar la foto utilizando
    // registerForActivityResult.
    private val tomarFoto =
    // Con registerForActivityResult() podemos manejar resultados
    // de imagenes.
    // Para el resultado se ejecuta cuando completamos
        // la actividad del metodo anterior.
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            // Validamos el resultado si es verdadero o falso.
            if (resultado.resultCode === Activity.RESULT_OK) {
                // Declaramos la variable de la foto con el Uri.
                val photoUri = classesCameraPhoto.getPhotoUri();

                // Obtenemos dentro del contexto del objeto
                // photoUri.
                photoUri?.let {
                    // Declaramos una variable para obtener el
                    // bitmap.
                    val bitmap = classesCameraPhoto.getBitmapFromUri(it);

                    // Aqui establecemos donde se pondra la imagen
                    // cuando tengamos la decodificacion y el proceso
                    // anterior realizado.
                    imageViewSelection.setImageBitmap(bitmap);
                }
            }
        }

    // Declarar como vamos a ingresar al almacenamiento
    // utilizando el registerForActivityResult.
    private val seleccionarFoto =
    // Con registerForActivityResult() podemos manejar resultados
    // de imagenes.
    // Para el resultado se ejecuta cuando completamos
        // la actividad del metodo anterior.
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            // Validamos el resultado si es verdadero o falso.
            if (resultado.resultCode == Activity.RESULT_OK) {
                // Declaramos la variable de la foto con el Uri.
                val imageUri = resultado.data?.data;

                // Obtenemos dentro del contexto del objeto
                // imageUri.
                imageUri?.let {
                    // Declaramos una variable para obtener el
                    // bitmap.
                    val bitmap = classesStorage.getBitmapFromUri(it);

                    // Aqui establecemos donde se pondra la imagen
                    // cuando tengamos la decodificacion y el proceso
                    // anterior realizado.
                    imageViewSelection.setImageBitmap(bitmap);
                }
            }
        }

    // Declarar variable del boton registrar.
    private lateinit var registerButton: AppCompatButton;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar firebase auth y store.
        auth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        // Inicializar la clase ClassesCameraPhoto.kt.
        classesCameraPhoto = ClassesCameraPhoto(this);

        // Inicializar la clase ClassesStorage.kt.
        classesStorage = ClassesStorage(this);

        // Obtenemos el id de la foto
        imageViewSelection = findViewById(R.id.imageViewSelection);

        // Obtener los valores de los campos de textos.
        nombreEditText = findViewById(R.id.nombreEditText);
        apellidoEditText = findViewById(R.id.apellidoEditText);
        telefonoEditText = findViewById(R.id.telefonoEditText);
        correoEditText = findViewById(R.id.correoEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // Obtener el valor del boton registrar.
        registerButton = findViewById(R.id.registerButton);

        // Manejamos el click del image view.

        imageViewSelection.setOnClickListener {
            mostrarModalOpciones();
        }

        // Cuando se haga click en el boton registrar.
        registerButton.setOnClickListener {
            // Convertir los campos de texto a
            // string.
            val nombre = nombreEditText.text.toString().trim();
            val apellido = apellidoEditText.text.toString().trim();
            val telefono = telefonoEditText.text.toString().trim();
            val correo = correoEditText.text.toString().trim();
            val password = passwordEditText.text.toString().trim();

            // Validar que los campos nos esten vacios.
            if (validateFields(nombre, apellido, telefono, correo, password)) {
                // Llevar los datos a firebase auth y firebase store.
                registerUser(nombre, apellido, telefono, correo, password)
            }
        }
    }

    private fun mostrarModalOpciones() {
        val view = layoutInflater.inflate(R.layout.dialog_modal_photos_options, null);

        val dialog = AlertDialog.Builder(this).setView(view).create();

        // Obtener los id's de las variables.
        tomarFotoTextView = view.findViewById(R.id.tomarFotoTextView);
        seleccionarFotoTextView = view.findViewById(R.id.seleccionarFotoTextView);

        // Opcion para tomar foto
        tomarFotoTextView.setOnClickListener {
            // Revisamos si el usuario permitio usar
            // la camara.
            if (classesCameraPhoto.checkCameraPermission(this)) {
                // Declaramos la variable para traer la funcion
                // dispatch de la clase camera foto.
                val takePictureIntent = classesCameraPhoto.dispatchTakePictureIntent()

                // Aqui ejecutando el tomarFoto podremos
                // iniciar y tomar la foto.
                if (takePictureIntent != null) {
                    tomarFoto.launch(takePictureIntent)
                }
            }
            // Lo que haremos despues de darle click.
            dialog.dismiss();
        }

        // Opcion para tomar imagen del almacenamiento.
        seleccionarFotoTextView.setOnClickListener {
            // Revisamos si el usuario permitio usar
            // el almacenamiento externo.
            if (classesStorage.checkStoragePermission(this)) {
                // Declaramos la variable para traer la funcion
                // dispatch de la clase camera foto.
                val selectImageIntent = classesStorage.createSelectImageIntent();

                // Aqui ejecutando el tomarFoto podremos
                // iniciar y tomar la foto.
                seleccionarFoto.launch(selectImageIntent);
            }

            // Lo que haremos despues de darle click.
            dialog.dismiss();
        }

        dialog.show();
    }

    // Se llama la funcion cuando el usuario responde
    // si acepta los permisos.
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Llamamos la funcion que declaramos en el archivo
        // ClassesCameraPhoto y le pasamos los parametros
        // necesarios.
        classesCameraPhoto.onRequestPermissionsResult(requestCode, grantResults, {
            // Declaramos la variable para traer la funcion
            // dispatch de la clase camera foto.
            val takePictureIntent = classesCameraPhoto.dispatchTakePictureIntent();

            // Aqui ejecutando el tomarFoto podremos
            // iniciar y tomar la foto.
            if (takePictureIntent != null) {
                tomarFoto.launch(takePictureIntent);
            }
        }, {
            // Si hubo un acceso denegado en los permisos
            // mostramos un toast de error.
            Toast.makeText(this, "Acceso Denegado!", Toast.LENGTH_LONG).show();
        });

        // Llamamos la funcion que declaramos en el archivo
        // ClassesStorage y le pasamos los parametros
        // necesarios.
        classesStorage.onRequestPermissionsResult(requestCode, grantResults, {
            // Declaramos la variable para traer la funcion
            // createSelectImageIntent de la ClassesStorage.
            val selectImageIntent = classesStorage.createSelectImageIntent();

            seleccionarFoto.launch(selectImageIntent);
        }, {
            // Si hubo un acceso denegado en los permisos
            // mostramos un toast de error.
            Toast.makeText(this, "Acceso Denegado!", Toast.LENGTH_LONG).show();
        });
    }

    // Validar que los campos esten llenos.
    private fun validateFields(
        nombre: String, apellido: String, telefono: String, correo: String, password: String
    ): Boolean {
        // Si alguno de los campos esta vacio.
        if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            // Entonces mostrar el siguiente toast.
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()

            // Retornar faslse.
            return false
        }

        // Si los campos estan bien retornar true.
        return true
    }

    // Funcion para registrar los usuarios.
    private fun registerUser(
        nombre: String, apellido: String, telefono: String, correo: String, password: String
    ) {
        // Guardar el correo y contraseña en
        // firbase auth.
        auth.createUserWithEmailAndPassword(correo, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si salio bien entonces declaramos
                // las siguientes variables.
                val user = auth.currentUser;
                val userId = user?.uid;

                // Usamos un hashMap para recoger los
                // valores de cada campo.
                val userMap = hashMapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "telefono" to telefono,
                    "correo" to correo,
                )

                // Si el usuario existe entonces continuamos.
                if (userId != null) {
                    // Guardamos todos los datos del usuario
                    // en una tabla/coleccion llamada users.
                    fireStore.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            // Mostramos un mensaje si fue correcto el
                            // ingreso de datos.
                            Toast.makeText(this, "Registro satisfactorio.", Toast.LENGTH_SHORT)
                                .show();

                            // Redirigimos a ActivitySignIn.
                            val intent = Intent(this, ActivitySignIn::class.java);
                            startActivity(intent);
                        }.addOnFailureListener { error ->
                            // Si hubo algun error mostrar el siguiente
                            // toast.
                            Toast.makeText(
                                this, "Error al registrarse. ${error.message}", Toast.LENGTH_SHORT
                            ).show();
                        }
                }
            }
        }
    }
}