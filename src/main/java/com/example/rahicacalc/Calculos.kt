package com.example.rahicacalc

fun calcularHidratos(gramos: Double, hidratosPor100g: Double): Double {
    return (hidratosPor100g * gramos) / 100
}

fun calcularRaciones(hidratosTotales: Double, hidratosPorRacion: Double = 10.0): Double {
    return hidratosTotales / hidratosPorRacion
}

