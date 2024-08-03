package com.example.proyecto_final_car_wash

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide

class PedidoAdapter(context: Context, private val pedidos: List<Pedido>) :
    ArrayAdapter<Pedido>(context, 0, pedidos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Declaramos el view de donde veremos toda esta informacion.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_pedido, parent, false);

        // Declaramos una variable pedio para saber donde ir poniendo la informacion.
        val pedido = pedidos[position];

        val vehiculoImageView = view.findViewById<ImageView>(R.id.vehiculoImageView);
        val vehiculoNameTextView = view.findViewById<TextView>(R.id.vehiculoNameTextView)

        vehiculoNameTextView.text = pedido.vehiculo;

        val imageUrl = when (pedido.vehiculo) {
            "Turismo" -> R.drawable.turismo
            "4x4" -> R.drawable.a4x4
            "Camion" -> R.drawable.camion
            "Bus" -> R.drawable.bus
            "Trailer" -> R.drawable.trailer

            else -> "../res/drawable/logo.png"
        }

        Glide.with(context).load(imageUrl).placeholder(R.drawable.logo).into(vehiculoImageView);

        val linearLayout = view.findViewById<LinearLayout>(R.id.linearLayout);

        val borderColor = if (pedido.confirmacion) {
            context.resources.getColor(R.color.confirmado);
        } else {
            context.resources.getColor(R.color.pendiente);
        }

        val backgroundDrawable = GradientDrawable().apply {
            setColor(Color.WHITE);
            cornerRadius = 12f;

            setStroke(4, borderColor);
        }

        linearLayout.background = backgroundDrawable;

        // Retornamos lo que veremos en el list view.
        return view;
    }
}