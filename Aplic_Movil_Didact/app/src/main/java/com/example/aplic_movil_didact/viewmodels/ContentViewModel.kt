package com.example.aplic_movil_didact.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplic_movil_didact.MpazApp
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.InformeEntity
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import com.example.aplic_movil_didact.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContentViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MpazApp).repository

    // ---------- Asignaturas / Unidades ----------
    fun getAsignaturas(soloHabilitadas: Boolean): Flow<List<AsignaturaEntity>> =
        repo.getAsignaturas(soloHabilitadas)

    fun getUnidades(asignaturaId: Int, soloHabilitadas: Boolean): Flow<List<UnidadEntity>> =
        repo.getUnidades(asignaturaId, soloHabilitadas)

    fun toggleAsignatura(asignatura: AsignaturaEntity) = viewModelScope.launch {
        repo.toggleAsignatura(asignatura)
    }

    fun toggleUnidad(unidad: UnidadEntity) = viewModelScope.launch {
        repo.toggleUnidad(unidad)
    }

    // ---------- Estudiantes ----------
    fun getEstudiantes(): Flow<List<UserEntity>> = repo.getEstudiantes()

    // ---------- Entregas ----------
    fun getEntregas(): Flow<List<EntregaEntity>> = repo.getEntregas()

    fun getEntregasDeEstudiante(email: String): Flow<List<EntregaEntity>> =
        repo.getEntregasDeEstudiante(email)

    fun getEntregaDeEstudiante(email: String, asignatura: String, unidad: String, tarea: String): Flow<EntregaEntity?> =
        repo.getEntregaDeEstudiante(email, asignatura, unidad, tarea)

    /** onResultado(false) = la tarea ya estaba entregada y no se guardó de nuevo. */
    fun entregarTarea(entrega: EntregaEntity, onResultado: (Boolean) -> Unit) = viewModelScope.launch {
        onResultado(repo.insertEntrega(entrega))
    }

    fun calificarEntrega(entrega: EntregaEntity, calificacion: Float, observacion: String) = viewModelScope.launch {
        repo.calificarEntrega(entrega, calificacion, observacion)
    }

    // ---------- Informes pedagógicos ----------
    fun getInformes(): Flow<List<InformeEntity>> = repo.getInformes()

    fun solicitarInforme(asignatura: String, indicaciones: String, solicitadoPor: String) = viewModelScope.launch {
        repo.solicitarInforme(asignatura, indicaciones, solicitadoPor)
    }

    fun responderInforme(informe: InformeEntity, contenido: String, respondidoPor: String) = viewModelScope.launch {
        repo.responderInforme(informe, contenido, respondidoPor)
    }
}
