package com.example.proyecto_final_car_wash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.proyecto_final_car_wash.databinding.FragmentMiPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

// Aqui es donde estaria el
// fragmento de mi perfil.
// La funcion onCreateView
// es generada automaticamente.
class FragmentMiPerfil : Fragment() {
    // Declarar el binding del Activity.
    private lateinit var binding: FragmentMiPerfilBinding;

    // Declaramos un activity.
    private lateinit var activity: AppCompatActivity;

    // Declarar las variables para firebase.
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Declaramos el imageViewSelection.
    private lateinit var imageViewSelection: ImageView;

    // Declaramos una variable imageUri a nulo.
    private var imageUri: Uri? = null;

    // Declaramos la propiedad requestPermissions de tipo
    // ActivityResultLauncher<Array<String>>.
    // La utilizamos para manejar el resultado de los
    // permisos que recibira.
    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>;

    // Primero declaramos una propiedad que sea de tipo
    // ActivityResultLauncher<String>.
    private lateinit var pickImage: ActivityResultLauncher<String>;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        pickImage =
                // Registramos el resultado que nos de, en este caso
                // sera una imagen.
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Cuando seleccionamos la imagen se ejecutara esta
                // parte del codigo.
                uri?.let {
                    imageUri = it;
                    // Establecemos la imagen en el imageViewSelection.
                    imageViewSelection.setImageURI(it);
                }
            }

        requestPermissions =
                // Aqui registramos la accion para solicitar los
                // permisos.
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
                // Aqui si ya los solicitamos entonces validamos lo que hara
                // el usuario si los aceptara o no.
                if (results.all { it.value }) {
                    // Si los acepta entonces podra ejecutar la funcion
                    // selectImage().
                    selectImage();
                } else {
                    // SI no acepta se mostrar que denego los permisos.
                    Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context);

        // Verificamos si el contexto es pasado como
        // argumento de la clase AppCompatActivity.
        if (context is AppCompatActivity) {
            // Si es asi inicalizamos activity como
            // el contexto.
            activity = context;
        } else {
            // Si el contexto no es una instancia
            // de AppCompatActivity mostramos un
            // mensaje de error.
            throw IllegalStateException("Activity must be AppCompatActivity");
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMiPerfilBinding.inflate(inflater, container, false);

        // Asignar a las variables de firebase.
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Inicializar imageViewSelection.
        imageViewSelection = binding.imageViewSelection;

        // Llamamos la funcion para cargar los datos.
        cargarUsuario()

        // Manejamos el click de salvar los cambios
        // con la funcion de guardar.
        binding.salvarCambiosButton.setOnClickListener {
            guardarPerfil()
        }

        // Manejamos el click para regresar.
        binding.regresarButton.setOnClickListener {
            // Llevamos al usuario al ActivitSignIn
            // por cuestiones de seguridad.
            val intent = Intent(requireContext(), ActivitySignIn::class.java)
            startActivity(intent)
        }

        // Manjeamos el click para seleccionar una foto.
        binding.imageViewSelection.setOnClickListener {
            requestPermissions();
        }

        return binding.root
    }

    // La funcion cargar usuario traera la informacion de firebase para mostrarla
    // al usuario.
    private fun cargarUsuario() {
        // Declaramos una variable para saber quien es el usuario
        // actual.
        val user = auth.currentUser;

        // Validamos que el usuario no sea nulo.
        if (user != null) {
            // De la coleccion users traemos la informacion que queremos
            // siempre y cuando conincida con el id del usuario.
            firestore.collection("users").document(user.uid).get()
                // Si la informacion fue traida correctamente continuamos.
                .addOnSuccessListener { document ->
                    // Validamos que trajo la informacion.
                    if (document != null) {
                        // Aqui declaramos los campos de texto donde queremos
                        // que se muestra la informacion.
                        binding.currentPasswordEditText.setText(document.getString("nombre"));
                        binding.newPasswordEditText.setText(document.getString("apellido"));
                        binding.telefonoLayout.editText?.setText(
                            document.getString("telefono")
                        );

                        // Cargar la imagen en el ImageView.
                        val imageUrl = document.getString("imageUrl");

                        // Si la imagen es nula y no esta vacia continuamos.
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            // Glide es una dependencia agregada al proyecto
                            // que permite hacer un fetch de las imagenes que le digamos.
                            // Aqui le decimos que traiga/cargue la imageUrl que le dimos.
                            Glide.with(this).load(imageUrl)
                                // Aqui seria declarar metodos de configuracion necesaria.
                                .apply(RequestOptions().override(Target.SIZE_ORIGINAL))
                                // Por ultimo le decimos donde quiere que la muestre.
                                .into(binding.imageViewSelection);
                        }
                    } else {
                        // Si los datos no se pudiero cargar entonces
                        // mostrar un error.
                        Toast.makeText(
                            context, "No se pudo cargar los datos.", Toast.LENGTH_SHORT
                        ).show();
                    }
                }.addOnFailureListener { exception ->
                    // Si la imagen no se pudo cargar mostrar un mensaje de error.
                    Toast.makeText(
                        context, "No se pudo cargar la imagen", Toast.LENGTH_SHORT
                    ).show();
                }
        }
    }

    // Esta sera la funcion que utlizaremos para
    // salvar los cambios al actualizar el perfil.
    private fun guardarPerfil() {
        // Primero declaramos una variable para guardar el usuario
        // actual.
        val user = auth.currentUser

        // Validamos si el usuario es nulo.
        if (user != null) {
            // Utilizamos la funcion subirImageFirebaseStorage
            // para guardar nuestra imagen en firebase storage
            // cloud.
            subirImageFirebaseStorage(onSuccess = { imageUrl ->
                // Si al subir la imagen fue exitoso
                // entonces decimos que en la coleccion de users
                // hacemos un get().
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { documentFoto ->
                        // Si salio bien traer la informacion con el get(),
                        // validamos si el documentoFoto existe.
                        if (documentFoto.exists()) {
                            // Declaramos una variable usuario para obtener
                            // y convertir el documento foto a un objeto.
                            // Lo traemos desde una clase aparte.
                            val usuario = documentFoto.toObject(Usuario::class.java);

                            // Aqui declaramos una variable para la imagen anterior que estaba
                            // antes al usuario.
                            val imageUrlAnterior = usuario?.imageUrl;

                            // Declaramos un hash map para almacenar todas las modificaciones
                            // que vamos a mandar a firebase.
                            val perfilUsuario = hashMapOf<String, Any>(
                                "nombre" to binding.currentPasswordEditText.text.toString(),
                                "apellido" to binding.newPasswordEditText.text.toString(),
                                "telefono" to binding.telefonoLayout.editText?.text.toString(),
                                "imageUrl" to imageUrl
                            );

                            // Aqui es donde actualizamos al usuario usando el metodo
                            // update().
                            firestore.collection("users").document(user.uid).update(perfilUsuario)
                                .addOnSuccessListener {
                                    // Si salio bien entonces confirmamos que haya una imagen
                                    // antes de eliminar, si no hay entonces no elimina nada.
                                    if (imageUrlAnterior.isNullOrEmpty()) {
                                        // Declaramos una variable para obtener la referencia
                                        // a la imagen anterior.
                                        val storageReference = FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(imageUrlAnterior!!);

                                        // Aqui le decimos que borre la imagen con el metodo
                                        // delete().
                                        storageReference.delete().addOnSuccessListener {
                                            // Si la imagen se borro correctamentemente mostramos
                                            // un mensaje o hacemos algo aqui.
                                            Toast.makeText(
                                                context,
                                                "Imagen anterior eliminada",
                                                Toast.LENGTH_SHORT
                                            ).show();
                                        }.addOnFailureListener { exception ->
                                            // Si la imagen no se borro correctamente entonces
                                            // mostrar un error.
                                            Toast.makeText(
                                                context,
                                                "Error al eliminar la imagen anterior. ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }

                                    // Aqui mostramos un mensaje cuando el perfil haya sido
                                    // actualizado.
                                    Toast.makeText(
                                        context, "Perfil actualizado.", Toast.LENGTH_SHORT
                                    ).show();
                                }.addOnFailureListener { error ->
                                    // Si hubo un error al actualizar el perfil mostramos un
                                    // mensaje de error.
                                    Toast.makeText(
                                        context,
                                        "Error al actualizar el perfil. ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }

                        }
                    }.addOnFailureListener { e ->
                        // Mostramos un mensaje de error si los datos del usuario no se pudiero
                        // traer con el get().
                        Toast.makeText(
                            context,
                            "Error al obtener los datos del usuario: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show();
                    }
            }, onFailure = { exception ->
                // Si la imagen de alguna manera no se pudo enviar entonces mostramos un error.
                Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Declaramos la funcion para soliciat permisos.
    private fun requestPermissions() {
        // Verificamos la version del android si es mayor a
        // UPSIDE_DOWN_CAKE.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Solicitamos los siguientes permisos para acceder
            // a un archivo de "Media".
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
            // Si la version es mayor o igual a la version
            // TIRAMISU realizamos este codigo.
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Solicitamos los siguientes permisos en forma de array.
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
            )
            // Si la version no es igual a las anteriores entonces
            // ejecutamos  solo un permiso.
        } else {
            // Solicitamos el permiso de leer el almacenamiento
            // externo.
            requestPermissions.launch(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    // Funcion para seleccionar un archivo de tipo imagen
    private fun selectImage() {
        // Manejamos el resultado de la imagen solicitada
        // y le decimos que solo puede escoger archivos
        // de tipo imagen.
        pickImage.launch("image/*")
    }

    // Funcion para subir la imagen a firebase cloud storage.
    fun subirImageFirebaseStorage(
        // Parametro para mensaje de exito.
        onSuccess: (String) -> Unit,
        // Parametro para mensaje erroneo.
        onFailure: (Exception) -> Unit
    ) {

        // Ejecutamos en el contexto del objeto
        // imageUri si no es nulo.
        imageUri?.let {
            // Esta variable primeramente le decimos que es de tipo
            // StorageReference.
            //  Tambien se inicializa con una referencia al almacenamiento
            // de firebase.
            val storageReference: StorageReference =
                FirebaseStorage.getInstance().reference.child("imagenesDePefil/${System.currentTimeMillis()}");

            // Le decimos que la variable anterior con el  putFile()
            // que seria el imageUri y le decimos que haya el
            // paso de exito y error.
            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                // Aqui obtenemos la URL de descarga del archivo.
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    // Aqui es simplemente un mensaje de exito
                    // convertido a string.
                    onSuccess(uri.toString());
                }
                // Si hubo algun error entonces mostrarlo.
            }.addOnFailureListener { exception ->
                onFailure(exception);
            }
        }
    }
}
