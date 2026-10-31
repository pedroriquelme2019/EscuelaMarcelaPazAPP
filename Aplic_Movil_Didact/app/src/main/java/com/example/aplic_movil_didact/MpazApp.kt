package com.example.aplic_movil_didact

import android.app.Application
import com.example.aplic_movil_didact.data.database.AppDatabase
import com.example.aplic_movil_didact.data.repository.ContentRepository

class MpazApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        ContentRepository(
            database.asignaturaDao(),
            database.unidadDao(),
            database.entregaDao(),
            database.informeDao(),
            database.userDao()
        )
    }
}
