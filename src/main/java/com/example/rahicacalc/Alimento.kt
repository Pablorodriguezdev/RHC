package com.example.rahicacalc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alimento")
data class Alimento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val hidratosPor100g: Double
)


