package com.example.proyecto_final_car_wash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class ClassesStorage(private val context: Context) {
    companion object {
        // Aqui como nos dice la variable
        // pedimos permiso para acceder a los
        // archivos.
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1002
    }

    fun checkStoragePermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                activity, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitamos los permisos de la camara
            // con el ActivityCompat.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            );

            // Devolvemos false como return false.
            false
        } else {
            // Si el if anterior no es verdadero
            // entonces retornamos true.
            true
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
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
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

    fun createSelectImageIntent(): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT);

        intent.type = "image/*"

        return intent;
    }
}