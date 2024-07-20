package com.example.proyecto_final_car_wash

data class Pedido(
    val fecha: String = "",
    val lavado: String = "",
    val numeroPedido: Int = 0,
    val total: Int = 0,
    val vehiculo: String = "",
    val servicio: String = "",
    val confirmacion: Boolean = false
);
