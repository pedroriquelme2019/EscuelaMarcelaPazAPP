package com.example.aplic_movil_didact.data.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aplic_movil_didact.data.Roles
import com.example.aplic_movil_didact.data.dao.AsignaturaDao
import com.example.aplic_movil_didact.data.dao.EntregaDao
import com.example.aplic_movil_didact.data.dao.InformeDao
import com.example.aplic_movil_didact.data.dao.UnidadDao
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.InformeEntity
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import com.example.aplic_movil_didact.data.local.dao.UserDao
import com.example.aplic_movil_didact.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        AsignaturaEntity::class,
        UnidadEntity::class,
        EntregaEntity::class,
        InformeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun asignaturaDao(): AsignaturaDao
    abstract fun unidadDao(): UnidadDao
    abstract fun entregaDao(): EntregaDao
    abstract fun informeDao(): InformeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { prePopulate(it) }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private suspend fun prePopulate(db: AppDatabase) {
            // Usuarios de prueba. El rol queda guardado como atributo del usuario
            // y además tiene que calzar con el dominio del correo.
            listOf(
                UserEntity(email = "ana.garcia@emp.cl", name = "Ana García", password = "123456", role = Roles.ESTUDIANTE),
                UserEntity(email = "carlos.lopez@emp.cl", name = "Carlos López", password = "123456", role = Roles.ESTUDIANTE),
                UserEntity(email = "maria.torres@emp.cl", name = "María Torres", password = "123456", role = Roles.ESTUDIANTE),
                UserEntity(email = "juan.perez@emp.cl", name = "Juan Pérez", password = "123456", role = Roles.ESTUDIANTE),
                UserEntity(email = "sofia.munoz@emp.cl", name = "Sofía Muñoz", password = "123456", role = Roles.ESTUDIANTE),
                UserEntity(email = "profesor@docente.cl", name = "Profesora 2° Básico A", password = "123456", role = Roles.DOCENTE, grade = "2° Básico A"),
                UserEntity(email = "utp@utp.cl", name = "Jefa UTP", password = "123456", role = Roles.UTP, grade = "")
            ).forEach { db.userDao().insertUser(it) }

            listOf(
                AsignaturaEntity(1, "Lenguaje y Comunicación", "Comprensión lectora y producción de textos"),
                AsignaturaEntity(2, "Matemática", "Números, operaciones y geometría"),
                AsignaturaEntity(3, "Ciencias Naturales", "El cuerpo humano, seres vivos y el entorno"),
                AsignaturaEntity(4, "Historia y Geografía", "Chile y el mundo en el tiempo")
            ).forEach { db.asignaturaDao().insert(it) }

            db.unidadDao().insertAll(
                UnidadEntity(asignaturaId = 1, titulo = "Unidad 1 · Comprensión Lectora"),
                UnidadEntity(asignaturaId = 1, titulo = "Unidad 2 · Producción de Textos"),
                UnidadEntity(asignaturaId = 2, titulo = "Unidad 1 · Números y Operaciones"),
                UnidadEntity(asignaturaId = 2, titulo = "Unidad 2 · Geometría"),
                UnidadEntity(asignaturaId = 3, titulo = "Unidad 1 · El Cuerpo Humano"),
                UnidadEntity(asignaturaId = 3, titulo = "Unidad 2 · Seres Vivos"),
                UnidadEntity(asignaturaId = 4, titulo = "Unidad 1 · Chile en el Tiempo"),
                UnidadEntity(asignaturaId = 4, titulo = "Unidad 2 · Geografía de Chile")
            )
        }
    }
}
