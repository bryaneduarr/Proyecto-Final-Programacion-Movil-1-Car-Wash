package com.example.proyecto_final_car_wash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto_final_car_wash.databinding.ActivityPedidoDomicilioBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import firebase.com.protolitewrapper.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class ActivityPedidoDomicilio : AppCompatActivity(), MapEventsReceiver {
    // Asignamos el binding.
    private lateinit var binding: ActivityPedidoDomicilioBinding;

    // Declaramos variables.
    private lateinit var mapView: MapView;
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentMarker: Marker? = null;

    private var latitude: Double = 0.0;
    private var longitude: Double = 0.0;

    // Variable para pedir permiso de localizacion.
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID;

        enableEdgeToEdge()
        setContentView(R.layout.activity_pedido_domicilio)
        // Asignando variables.
        binding = ActivityPedidoDomicilioBinding.inflate(layoutInflater);
        setContentView(binding.root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos las variables del mapa.
        mapView = binding.mapView;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        val mapEventsOverlay = MapEventsOverlay(this);
        mapView.overlays.add(mapEventsOverlay);


        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            );
        } else {
            obtenerUbicacion();
        }

        val vehiculo = intent.getStringExtra("vehiculo");
        val lavado = intent.getStringExtra("lavado");
        val servicio = intent.getStringExtra("servicio");
        val fecha = intent.getStringExtra("fecha");
        val precioVehiculo = intent.getDoubleExtra("precioVehiculo", 0.00);
        val precioLavado = intent.getDoubleExtra("precioLavado", 0.00);
        val precioServicio = intent.getDoubleExtra("precioServicio", 0.00);
        val total = intent.getDoubleExtra("total", 0.00);

        Log.d("Precio Vehiculo: ", precioVehiculo.toString());
        Log.d("Precio Lavado: ", precioLavado.toString());
        Log.d("Precio Servicio: ", precioServicio.toString());

        binding.continuarPedidoCotizacionButton.setOnClickListener {
            val intent = Intent(this, ActivityPedidoTotal::class.java).apply {
                putExtra("vehiculo", vehiculo);
                putExtra("lavado", lavado);
                putExtra("servicio", servicio);
                putExtra("fecha", fecha);
                putExtra("precioVehiculo", precioVehiculo);
                putExtra("precioLavado", precioLavado);
                putExtra("precioServicio", precioServicio);
                putExtra("total", total);
                putExtra("latitud", latitude);
                putExtra("longitud", longitude);
            };

            startActivity(intent);
        }

        binding.regresarButton.setOnClickListener {
            // Redirigimos a ActivitySolicCotizacion.kt.
            val intent = Intent(this, ActivitySolicCotizacion::class.java);
            startActivity(intent);
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                obtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso denegado!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun obtenerUbicacion() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                latitude = it.latitude;
                longitude = it.longitude;

                currentLocationOnMap(latitude, longitude);
            }
        }
    }

    private fun currentLocationOnMap(latitude: Double, longitude: Double) {
        val mapController = mapView.controller;

        mapController.setZoom(15.0);

        val startPoint = GeoPoint(latitude, longitude);

        mapController.setCenter(startPoint);

        val startMaker = Marker(mapView);

        startMaker.position = startPoint;

        startMaker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapView.overlays.add(startMaker);

        currentMarker = startMaker;
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        p?.let {
            currentMarker?.let { mapView.overlays.remove(it) }

            val newMarker = Marker(mapView);

            newMarker.position = it;

            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            mapView.overlays.add(newMarker);

            currentMarker = newMarker;

            latitude = it.latitude;
            longitude = it.longitude;

            Log.d("Selected Location", "Lat: $latitude, Lon: $longitude");
        }

        return true;
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false;
    }
}