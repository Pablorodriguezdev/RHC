package com.example.rahicacalc

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataAlimentos {

    // Esta lista se inicializa vacía y se carga con load()
    var lista: List<Alimento> = emptyList()
        private set

    fun load(context: Context) {
        val inputStream = context.resources.openRawResource(R.raw.alimentos)
        val json = inputStream.bufferedReader().use { it.readText() }
        val tipoLista = object : TypeToken<List<Alimento>>() {}.type
        lista = Gson().fromJson(json, tipoLista)
    }
}

