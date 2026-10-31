package com.example.aplic_movil_didact.data.repository

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
import kotlinx.coroutines.flow.Flow

class ContentRepository(
    private val asignaturaDao: AsignaturaDao,
    private val unidadDao: UnidadDao,
    private val entregaDao: EntregaDao,
    private val informeDao: InformeDao,
    private val userDao: UserDao
) {
    // ---------- Asignaturas / Unidades ----------
    fun getAsignaturas(soloHabilitadas: Boolean): Flow<List<AsignaturaEntity>> =
        if (soloHabilitadas) asignaturaDao.getHabilitadas() else asignaturaDao.getAll()

    fun getUnidades(asignaturaId: Int, soloHabilitadas: Boolean): Flow<List<UnidadEntity>> =
        if (soloHabilitadas) unidadDao.getHabilitadasByAsignatura(asignaturaId)
        else unidadDao.getAllByAsignatura(asignaturaId)

    suspend fun toggleAsignatura(asignatura: AsignaturaEntity) =
        asignaturaDao.update(asignatura.copy(habilitada = !asignatura.habilitada))

    suspend fun toggleUnidad(unidad: UnidadEntity) =
        unidadDao.update(unidad.copy(habilitada = !unidad.habilitada))

    // ---------- Usuarios ----------
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)

    fun getEstudiantes(): Flow<List<UserEntity>> = userDao.getUsersByRole(Roles.ESTUDIANTE)

    // ---------- Entregas ----------
    fun getEntregas(): Flow<List<EntregaEntity>> = entregaDao.getAll()

    fun getEntregasDeEstudiante(email: String): Flow<List<EntregaEntity>> = entregaDao.getByEstudiante(email)

    fun getEntregaDeEstudiante(email: String, asignatura: String, unidad: String, tarea: String): Flow<EntregaEntity?> =
        entregaDao.getEntregaDeEstudiante(email, asignatura, unidad, tarea)

    /** Devuelve false si la tarea ya estaba entregada (no se permite volver a subirla). */
    suspend fun insertEntrega(entrega: EntregaEntity): Boolean {
        val yaExiste = entregaDao.contarEntregas(
            entrega.estudianteEmail, entrega.asignatura, entrega.unidadTitulo, entrega.tareaTitle
        ) > 0
        if (yaExiste) return false
        return entregaDao.insert(entrega) != -1L
    }

    suspend fun calificarEntrega(entrega: EntregaEntity, calificacion: Float, observacion: String) =
        entregaDao.update(entrega.copy(calificacion = calificacion, observacion = observacion, calificada = true))

    // ---------- Informes pedagógicos ----------
    fun getInformes(): Flow<List<InformeEntity>> = informeDao.getAll()

    suspend fun solicitarInforme(asignatura: String, indicaciones: String, solicitadoPor: String) =
        informeDao.insert(InformeEntity(asignatura = asignatura, indicaciones = indicaciones, solicitadoPor = solicitadoPor))

    suspend fun responderInforme(informe: InformeEntity, contenido: String, respondidoPor: String) =
        informeDao.update(
            informe.copy(
                contenido = contenido,
                respondidoPor = respondidoPor,
                fechaRespuesta = System.currentTimeMillis(),
                enviado = true
            )
        )
}
