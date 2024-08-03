package com.example.proyecto_final_car_wash

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import firebase.com.protolitewrapper.BuildConfig
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ActivityVerUbicacionPedido : AppCompatActivity() {
    private lateinit var regresarButton: ImageButton;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID;


        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_ubicacion_pedido)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        regresarButton = findViewById(R.id.regresarButton);

        val latitud = intent.getDoubleExtra("latitud", 0.0);
        val longitud = intent.getDoubleExtra("longitud", 0.0);

        val mapView = findViewById<MapView>(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);

        val mapController = mapView.controller;

        mapController.setZoom(15.0);

        val startPoint = GeoPoint(latitud, longitud);

        mapController.setCenter(startPoint);

        val startMarker = Marker(mapView);

        startMarker.position = startPoint;

        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapView.overlays.add(startMarker);

        regresarButton.setOnClickListener {
            // Redirigimos a ActivityPedidoTotal.kt.
            finish()
        }
    }
}