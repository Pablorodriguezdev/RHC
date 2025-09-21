package com.example.rahicacalc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlimentoDao {
    @Query("SELECT * FROM alimento WHERE nombre LIKE :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Alimento?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlimentos(lista: List<Alimento>)
}

