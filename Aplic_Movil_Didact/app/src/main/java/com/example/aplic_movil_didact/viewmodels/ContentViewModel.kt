package com.example.aplic_movil_didact.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplic_movil_didact.MpazApp
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.UnidadEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ContentViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as MpazApp).repository

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

    fun getEntregas(): Flow<List<EntregaEntity>> = repo.getEntregas()

    fun insertEntrega(entrega: EntregaEntity) = viewModelScope.launch {
        repo.insertEntrega(entrega)
    }

    fun calificarEntrega(entrega: EntregaEntity, calificacion: Float, observacion: String) = viewModelScope.launch {
        repo.calificarEntrega(entrega, calificacion, observacion)
    }
}
