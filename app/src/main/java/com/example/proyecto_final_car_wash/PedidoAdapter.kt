package com.example.proyecto_final_car_wash

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PedidoAdapter(context: Context, private val pedidos: List<Pedido>) :
    ArrayAdapter<Pedido>(context, 0, pedidos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Declaramos el view de donde veremos toda esta informacion.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_pedido, parent, false);

        // Declaramos una variable pedio para saber donde ir poniendo la informacion.
        val pedido = pedidos[position];

        // Declaramos todas las variables de nuestro list_item_pedido.
        val tipoLavadoTextView = view.findViewById<TextView>(R.id.tipoLavadoTextView)
        val numeroPedidoTextView = view.findViewById<TextView>(R.id.numeroPedidoTextView)
        val vehiculoTextView = view.findViewById<TextView>(R.id.vehiculoTextView);
        val confirmacionTextView = view.findViewById<TextView>(R.id.confirmacionTextView);
        val fechaTextView = view.findViewById<TextView>(R.id.fechaTextView);
        val servicioTextView = view.findViewById<TextView>(R.id.servicioTextView);
        val totalTextView = view.findViewById<TextView>(R.id.totalTextView);

        // Seteamos los valores que tendra cada uno.
        tipoLavadoTextView.text = "Tipo de lavado: ${pedido.lavado}";
        numeroPedidoTextView.text = "Numero de Pedido: ${pedido.numeroPedido}";
        vehiculoTextView.text = "Vehiculo: ${pedido.vehiculo}";
        if (pedido.confirmacion) {
            confirmacionTextView.text = "Confirmacion de su pedido: Servicio entregado";

            confirmacionTextView.setTextColor(Color.parseColor("#459659"));
        } else {
            confirmacionTextView.text = "Confirmacion de su pedido: En espera";

            confirmacionTextView.setTextColor(Color.parseColor("#c4b25a"));
        }
        fechaTextView.text = "Fecha: ${pedido.fecha}";
        servicioTextView.text = "Tipo de servicio: ${pedido.servicio}";
        totalTextView.text = "Total: ${pedido.total}"


        // Retornamos lo que veremos en el list view.
        return view;
    }
}