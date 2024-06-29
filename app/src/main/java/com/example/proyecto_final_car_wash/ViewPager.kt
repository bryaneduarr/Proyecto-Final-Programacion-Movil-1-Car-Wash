package com.example.proyecto_final_car_wash

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    // Importante para el fragmentActivity
    // decirle cuantas secciones abran.
    override fun getItemCount(): Int = 2;

    // Aqui es donde creamos los tabs y
    // le decimos cuales seran los fragmentos
    // que usaremos y sus pocisiones.
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            // Aqui traemos los fragmentos
            // dependiendo como se llamen nuestros
            // archivos.
            0 -> FragmentMiPerfil()
            1 -> FragmentSeguridad()

            // Si hay un error entonces le decimos
            // que el tab no existe.
            else -> throw IllegalStateException("No existe el tab.")
        }
    }
}