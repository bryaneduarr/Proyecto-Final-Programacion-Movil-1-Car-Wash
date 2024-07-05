package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyecto_final_car_wash.databinding.FragmentSeguridadBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

// Aqui es donde estaria el
// fragmento de seguridad.
// La funcion onCreateView
// es generada automaticamente.
class FragmentSeguridad : Fragment() {
    // Declarar los binding necearios
    // para hacer funcionarlos en el
    // fragment.
    private var binding_: FragmentSeguridadBinding? = null;
    private val binding get() = binding_!!;

    // Declarar variables de firebase.
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Aqui declaramos el binding_ para incializarlo
        // y poder usar los id de xml facilmente.
        binding_ = FragmentSeguridadBinding.inflate(inflater, container, false);
        return binding.root;
    }

    // Funcion por defecto para poner nuestro
    // codigo.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        // Incializar firebase.
        auth = Firebase.auth;
        db = FirebaseFirestore.getInstance();

        // Cuando demos click al boton
        // regresar hara lo siguiente.
        binding.regresarButton.setOnClickListener {
            // Llevamos al usuario al ActivitSignIn
            // por cuestiones de seguridad.
            val intent = Intent(requireContext(), ActivitySignIn::class.java);
            startActivity(intent);
        }

        // Manejamos el click para cambiar de correo.
        binding.cambiarCorreoButton.setOnClickListener {
            cambiarCorreo();
        }

        // Manejamos cuando el usuario quiera borrar
        // su cuenta totalmente.
        binding.eliminarCuentaButton.setOnClickListener {
            val usuario = auth.currentUser;


            // Usamos let para decir que lo siguiente
            // sera dentro del contexto de este objeto
            // que seria el usuario y declarar variables.
            usuario?.let { user ->
                // Creamos una variable que puede cambiar
                // para obtener el correo del usuario.
                val correoUsuario = user.email;

                // Declaramos variables de firebaseFirestore
                // para elimnar el documento relacionado.
                val db = FirebaseFirestore.getInstance();
                val usersCollection = db.collection("users");

                // Usamos let para decir que lo siguiente
                // sera dentro del contexto de este objeto
                // que seria el correoUsuario en este caso.
                correoUsuario?.let { email ->
                    // Buscamos en la coleccion el campo correo
                    // con el correo del usuario hasta que sean
                    // similares.
                    usersCollection.whereEqualTo("correo", email).get()
                        .addOnSuccessListener { documents ->
                            // Si salio bien entonces vamos
                            // a iterar en los documentos y
                            // buscar el documento id y borrarlo.
                            for (document in documents) {
                                usersCollection.document(document.id).delete()
                                    .addOnCompleteListener { tarea ->
                                        // Si la tarea anterior fue correcta
                                        // podemos continuar.
                                        if (tarea.isSuccessful) {
                                            // Le decimos que vamos a borrar el usuario
                                            // actual.
                                            user.delete().addOnCompleteListener { borrarTarea ->
                                                // Revisamos si salio bien o mal.
                                                if (borrarTarea.isSuccessful) {
                                                    // Si salio bien entonces mostramos un
                                                    // mensaje de exito.
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Cuenta borrada exitosamente",
                                                        Toast.LENGTH_LONG
                                                    ).show();

                                                    auth.signOut();

                                                    // Y nos vamos al ActivitySignIn.
                                                    val intent = Intent(
                                                        requireContext(), ActivitySignIn::class.java
                                                    );
                                                    startActivity(intent);

                                                    // Mostrar mensaje para volver a iniciar
                                                    // sesion
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Porfavor vuevla a iniciar sesion.",
                                                        Toast.LENGTH_SHORT
                                                    ).show();
                                                } else {
                                                    // Si salio mal entonces mostramos un
                                                    // mensaje de error.
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Cuenta borrada exitosamente",
                                                        Toast.LENGTH_LONG
                                                    ).show();
                                                }
                                            }
                                        } else {
                                            // Si hubo un error al borrar los datos
                                            // de la coleccion mostrar un mensaje de error.
                                            Toast.makeText(
                                                requireContext(),
                                                "Hubo un error al borrar los datos: ${tarea.exception.toString()}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }.addOnFailureListener { error ->
                            // Si hubo un error al buscar
                            // los datos dentro de la coleccion
                            // mostrar un mensaje de error.
                            Toast.makeText(
                                requireContext(),
                                "Error al buscar los datos: ${error.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        // Cuando demos click revisaremos
        // haremos cambio de contraseña.
        binding.cambiarPasswordButton.setOnClickListener {
            // Declaramos la variable para el usuario
            // actual.
            val usuario = auth.currentUser;

            // Obtenemos los valores de los campos de
            // contraseña como string.
            val currentPassword = binding.currentPasswordEditText.text.toString();
            val nuevoPassword = binding.newPasswordEditText.text.toString();

            // Revisamos primeramente que los campos
            // no esten vacios.
            if (revisarCamposVacios()) {
                // Si el usuario no es nulo
                // osea si esta registrado continuar.
                if (usuario != null && currentPassword.isNotEmpty()) {
                    val credentials =
                        EmailAuthProvider.getCredential(usuario.email!!, currentPassword);

                    // Aqui volvemos a reautenticar al usuario
                    // con sus credenciales de correo y contraseña.
                    usuario.reauthenticate(credentials).addOnCompleteListener { task ->
                        // Si sus credenciales con correctas entonces
                        // continuar.
                        if (task.isSuccessful) {
                            // Aqui le decimos que cambie del usuario
                            // actual la contraseña que tiene actualmente.
                            usuario.updatePassword(nuevoPassword)?.addOnCompleteListener {
                                // Revisar si salio bien el cambio.
                                if (it.isSuccessful) {
                                    // Mostrar mensaje de exito.
                                    Toast.makeText(
                                        requireContext(),
                                        "Cambio de contraseña exitoso.",
                                        Toast.LENGTH_SHORT
                                    ).show();

                                    // Cerramos la sesion.
                                    auth.signOut();

                                    // Y nos vamos al ActivitySignIn.
                                    val intent =
                                        Intent(requireContext(), ActivitySignIn::class.java);
                                    startActivity(intent);

                                    // Mostrar mensaje para volver a iniciar
                                    // sesion
                                    Toast.makeText(
                                        requireContext(),
                                        "Porfavor vuevla a iniciar sesion.",
                                        Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    // Mostrar mensaje de error,
                                    // si hubo alguno.
                                    Toast.makeText(
                                        requireContext(),
                                        "Cambio de contraseña erroneo, ${it.exception.toString()}",
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        } else {
                            // Si la contraseña es incorrecta entonces
                            // mostrar un mensaje de error.
                            Toast.makeText(
                                requireContext(),
                                "Contraseña actual incorrecta.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // Mostrar mensaje de error si
                    // el usuario no esta registrado
                    // correctamente.
                    Toast.makeText(requireContext(), "Usuario incorrecto!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                // Mostrar mensaje de error si los campos
                // estan vacios.
                Toast.makeText(
                    requireContext(), "Porfavor llene todos los campos.", Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private fun cambiarCorreo() {
        // Crear una variable para almacenar el usuario actual.
        val usuario = auth.currentUser;

        // Obtener el campo del nuevo correo en forma de
        // string.
        val newEmail = binding.correoEditText.text.toString();

        // Obtener el cambo para confirmar la contraseña actual
        // (del correo).
        val confirmarPassword = binding.currentPasswordCorreoEditText.text.toString();

        // Manejar los campos vacios.
        if (newEmail.isEmpty() || confirmarPassword.isEmpty()) {
            // Mensaje si alguno de los campos estan vacios.
            Toast.makeText(
                requireContext(), "Por favor, llena todos los campos.", Toast.LENGTH_SHORT
            ).show();

            // Retornamos para no continuar.
            return;
        }

        // Crea las credenciales para volver a autenticar.
        val credential = EmailAuthProvider.getCredential(usuario?.email!!, confirmarPassword)

        // Re-autenticar al usuario.
        usuario.reauthenticate(credential).addOnCompleteListener { task ->
            // Manejar si el usuario se re-autentico exitosamente o no.
            if (task.isSuccessful) {
                // Si la re-autenticación es exitosa entonces enviamos un correo
                // al nuevo correo que ingreso el usuario para confirmar que exista
                // y que de verdad quiere cambiar.
                usuario.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { updateTask ->
                    // Si el correo se mando correctamente entonces mostrar
                    // un mensaje diciendo que verifique el correo que le mandamos
                    // al NUEVO CORREO QUE EL INGRESO.
                    if (updateTask.isSuccessful) {
                        // Mostrar mensaje de exito.
                        Toast.makeText(
                            requireContext(),
                            "Correo de verificación enviado. Por favor, verifica tu nuevo correo para completar el cambio.",
                            Toast.LENGTH_SHORT
                        ).show();

                        // Llamamos la siguiente funcion para cambiar el correo
                        // en firestore tambien.
                        actualizarCorreoEnFirestore(usuario.uid, newEmail);

                        // Cerramos sesion para que inicie sesion con el
                        // nuevo correo.
                        auth.signOut();

                        // Llevamos al usuario al ActivitySignIn.kt ya que cambio
                        // informacion importante asi que debe volver a iniciar sesion.
                        val intent = Intent(requireContext(), ActivitySignIn::class.java);
                        startActivity(intent);

                        // Mostramos un mensaje para que vuelva a iniciar sesion.
                        Toast.makeText(
                            requireContext(),
                            "Por favor vuevla a iniciar sesion con el nuevo correo.",
                            Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        // Si el correo no se pudo enviar entonces mostrar
                        // el siguiente mensaje.
                        Toast.makeText(
                            requireContext(),
                            "Error al enviar correo de verificación.",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
                }
            } else {
                // Si el usuario no se re-autentico de manera
                // correcta mostrar el siguiente mensaje.
                Toast.makeText(
                    requireContext(),
                    "Error de autenticación. ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private fun actualizarCorreoEnFirestore(userId: String, newEmail: String) {
        // Declaramos una variable de base de datos para
        // inicializar.
        val db = FirebaseFirestore.getInstance();

        // Obtenemos la direccion dentro de la coleccion
        // users y el id del usuario en una variable.
        val userRef = db.collection("users").document(userId);

        userRef.update("correo", newEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // No mostrar nada para no confundir al usuario.
            } else {
                // Si algo sale mal decirle que no se actualizo en Firestore.
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar correo en Firestore. ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun revisarCamposVacios(): Boolean {
        // Revisar que los campos de contraseña no esten
        // vacios y retornamos true o false dependiendo
        // el caso.
        return binding.currentPasswordEditText.text!!.isNotEmpty() && binding.newPasswordEditText.text!!.isNotEmpty();
    }

    // Funcion importante para cuando
    // salgamos del fragment y a la hora
    // de abrirlo sea funcional.
    override fun onDestroyView() {
        super.onDestroyView();

        binding_ = null;
    }
}