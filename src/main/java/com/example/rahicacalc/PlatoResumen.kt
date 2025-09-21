package com.example.rahicacalc



data class PlatoResumen(
    val fechaHora: String,
    val nombrePlato: String,
    val totalHidratos: Double,
    val totalRaciones: Double,
    val ingredientes: List<IngredienteCalculado>
)
