package com.example.proyecto_final_car_wash

import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ClassesStoragePhotos(
    // La usamos para representar en que actividad
    // ejecutaremos los permisos.
    private val activity: AppCompatActivity,
    // Esta es la propiedad en el activity
    // SignUpActivity.kt, la usamos para poder decirle
    // que ahi pondremos la imagen.
    private val imageViewSelection: ImageView
) {
    var imageUri: Uri? = null;

    // Primero declaramos una propiedad que sea de tipo
    // ActivityResultLauncher<String>.
    private val pickImage: ActivityResultLauncher<String> =
    // Registramos el resultado que nos de, en este caso
        // sera una imagen.
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Cuando seleccionamos la imagen se ejecutara esta
            // parte del codigo.
            uri?.let {
                imageUri = it;
                // Establecemos la imagen en el imageViewSelection.
                imageViewSelection.setImageURI(it)
            }
        }

    // Declaramos la propiedad requestPermissions de tipo
    // ActivityResultLauncher<Array<String>>.
    // La utilizamos para manejar el resultado de los
    // permisos que recibira.
    private val requestPermissions: ActivityResultLauncher<Array<String>> =
    // Aqui registramos la accion para solicitar los
        // permisos.
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Aqui si ya los solicitamos entonces validamos lo que hara
            // el usuario si los aceptara o no.
            if (results.all { it.value }) {
                // Si los acepta entonces podra ejecutar la funcion
                // selectImage().
                selectImage()
            } else {
                // SI no acepta se mostrar que denego los permisos.
                Toast.makeText(activity, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

    // Declaramos la funcion para soliciat permisos.
    fun requestPermissions() {
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