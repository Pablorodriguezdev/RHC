package com.example.rahicacalc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial")
data class HistorialEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fechaHora: String,
    val nombrePlato: String,
    val alimento: String,
    val hidratos: Double,
    val raciones: Double,
    val gramos: Double
)

