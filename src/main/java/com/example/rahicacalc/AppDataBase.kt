package com.example.rahicacalc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Alimento::class, HistorialEntry::class],
    version = 5, // o la que uses ahora
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alimentoDao(): AlimentoDao
    abstract fun historialDao(): HistorialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hidratos_db"
                )
                    .fallbackToDestructiveMigration() // 🔹 borra y recrea la DB si cambia la versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}




