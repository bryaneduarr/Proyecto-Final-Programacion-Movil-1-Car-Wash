package com.example.proyecto_final_car_wash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

// Aqui es donde estaria el
// fragmento de mi perfil.
// La funcion onCreateView
// es generada automaticamente.
class FragmentMiPerfil : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Aqui retornamos de donde sacamos
        // nuestro xml para decirle que habra dentro.
        return inflater.inflate(R.layout.fragment_mi_perfil, container, false);
    }
}