package com.example.aplic_movil_didact.data.repository

import com.example.aplic_movil_didact.data.dao.AsignaturaDao
import com.example.aplic_movil_didact.data.dao.EntregaDao
import com.example.aplic_movil_didact.data.dao.UnidadDao
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow

class ContentRepository(
    private val asignaturaDao: AsignaturaDao,
    private val unidadDao: UnidadDao,
    private val entregaDao: EntregaDao
) {
    fun getAsignaturas(soloHabilitadas: Boolean): Flow<List<AsignaturaEntity>> =
        if (soloHabilitadas) asignaturaDao.getHabilitadas() else asignaturaDao.getAll()

    fun getUnidades(asignaturaId: Int, soloHabilitadas: Boolean): Flow<List<UnidadEntity>> =
        if (soloHabilitadas) unidadDao.getHabilitadasByAsignatura(asignaturaId)
        else unidadDao.getAllByAsignatura(asignaturaId)

    suspend fun toggleAsignatura(asignatura: AsignaturaEntity) =
        asignaturaDao.update(asignatura.copy(habilitada = !asignatura.habilitada))

    suspend fun toggleUnidad(unidad: UnidadEntity) =
        unidadDao.update(unidad.copy(habilitada = !unidad.habilitada))

    fun getEntregas(): Flow<List<EntregaEntity>> = entregaDao.getAll()

    suspend fun insertEntrega(entrega: EntregaEntity) = entregaDao.insert(entrega)

    suspend fun calificarEntrega(entrega: EntregaEntity, calificacion: Float, observacion: String) =
        entregaDao.update(entrega.copy(calificacion = calificacion, observacion = observacion, calificada = true))
}
