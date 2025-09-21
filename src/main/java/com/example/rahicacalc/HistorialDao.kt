package com.example.rahicacalc


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Dao
interface HistorialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entry: HistorialEntry)

    // Consulta base sin ingredientes
    @Query("""
        SELECT fechaHora, nombrePlato, SUM(hidratos) AS totalHidratos, SUM(raciones) AS totalRaciones
        FROM historial
        GROUP BY fechaHora, nombrePlato
        ORDER BY MAX(id) DESC
    """)
    fun obtenerPlatosBase(): Flow<List<PlatoResumenSinIngredientes>>

    @Query("""
        SELECT fechaHora, nombrePlato, SUM(hidratos) AS totalHidratos, SUM(raciones) AS totalRaciones
        FROM historial
        WHERE fechaHora LIKE :fecha || '%'
        GROUP BY fechaHora, nombrePlato
        ORDER BY MAX(id) DESC
    """)
    fun obtenerPlatosPorFechaBase(fecha: String): Flow<List<PlatoResumenSinIngredientes>>

    // Ingredientes de un plato concreto
    @Query("""
        SELECT alimento AS nombre, gramos, hidratos, raciones
        FROM historial
        WHERE nombrePlato = :nombrePlato AND fechaHora = :fechaHora
    """)
    suspend fun obtenerIngredientes(nombrePlato: String, fechaHora: String): List<IngredienteCalculado>

    // Versión enriquecida que usa PlatoResumen directamente
    fun obtenerPlatos(): Flow<List<PlatoResumen>> =
        obtenerPlatosBase().map { lista ->
            lista.map { resumen ->
                val ingredientes = runCatching {
                    withContext(Dispatchers.IO) {
                        obtenerIngredientes(resumen.nombrePlato, resumen.fechaHora)
                    }
                }.getOrDefault(emptyList())

                PlatoResumen(
                    nombrePlato = resumen.nombrePlato,
                    fechaHora = resumen.fechaHora,
                    totalHidratos = resumen.totalHidratos,
                    totalRaciones = resumen.totalRaciones,
                    ingredientes = ingredientes
                )
            }
        }

    fun obtenerPlatosPorFecha(fecha: String): Flow<List<PlatoResumen>> =
        obtenerPlatosPorFechaBase(fecha).map { lista ->
            lista.map { resumen ->
                val ingredientes = runCatching {
                    withContext(Dispatchers.IO) {
                        obtenerIngredientes(resumen.nombrePlato, resumen.fechaHora)
                    }
                }.getOrDefault(emptyList())

                PlatoResumen(
                    nombrePlato = resumen.nombrePlato,
                    fechaHora = resumen.fechaHora,
                    totalHidratos = resumen.totalHidratos,
                    totalRaciones = resumen.totalRaciones,
                    ingredientes = ingredientes
                )
            }
        }
}




