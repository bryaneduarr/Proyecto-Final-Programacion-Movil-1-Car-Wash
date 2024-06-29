package com.example.proyecto_final_car_wash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ClassesCameraPhoto(private val context: Context) {
    companion object {
        // Aqui como nos dice la variable
        // pedimos permiso para tomar
        // la foto.
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001;
    }

    private var photoURI: Uri? = null;

    // Funcion estandar de la documentacion oficial.
    fun dispatchTakePictureIntent(): Intent? {
        val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.packageManager) != null) {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                photoURI = FileProvider.getUriForFile(
                    context, "com.example.codigo_en_clase.fileprovider", it
                )
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI)
                return takePictureIntent
            }
        }
        return null
    }

    // Funcion estandar de la documentacion oficial.
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    // Funcion utlizada para poner la imagen
    // en el imageView, devuelve un objeto
    // Bitmap o null.
    fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            // Con el context accedemos
            // al contexto actual y accedemos
            // a los datos de la imagen en este
            // caso.
            // Abrimos un flujo de entrada con openInputStream.
            // Con el .use { inputStream -> ... se ejecuta
            // si hay un flujo de entrada y decodificamos
            // el Bitmap.
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Usando el try vamos a intentar
                // decodificar el flujo del bitmap.
                // Aqui decodificamos.
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (error: IOException) {
            // Si ocurre algun error en el que no
            // se puede decodificar mostrar el error.
            error.printStackTrace();

            // Aqui estamos diciendo como
            // return null;.
            null;
        }
    }

    // Verificamos si los permisos de la camara fueron otorgados.
    fun checkCameraPermission(activity: Activity): Boolean {
        // Retornamos un booleano.
        // Si el ContextCompat.checkSelfPermission esta otorgado
        // y es diferente del PackageManager.PERMISSION_GRANTED
        // continuamos.
        return if (ContextCompat.checkSelfPermission(
                activity, android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitamos los permisos de la camara
            // con el ActivityCompat.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            );

            // Devolvemos false como return false.
            false;
        } else {
            // Si el if anterior no es verdadero
            // entonces retornamos true.
            true;
        }
    }

    // Manejamos la solicitud de permisos en este
    // caso de la camara.
    fun onRequestPermissionsResult(
        // Codigo para solicitar los permisos.
        requestCode: Int,
        // Array de (Int) para revisar si los
        // permisos fueron otorgados.
        grantResults: IntArray,
        // Funcion para saber si los ocupamos los
        // permisos
        onPermissionGranted: () -> Unit,
        //Funcion para saber si no fueron ejecutados
        // y los permisos son denegados.
        onPermissionDenied: () -> Unit
    ) {
        // Revisamos si los permisos de la camara fueron
        // otorgados.
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Aqui seguimos revisando si los permisos fueron
            // otorgados correctamente.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si es asi entonces llamamos la funcion del parametro
                // onPermissionGranted: () -> Unit,.
                onPermissionGranted()
            } else {
                // Si fueron denegados llamamos la funcion del parametro
                // onPermissionDenied: () -> Unit.
                onPermissionDenied()
            }
        }
    }

    var currentPhotoPath: String? = null

    fun getPhotoUri(): Uri? {
        return photoURI
    }
}