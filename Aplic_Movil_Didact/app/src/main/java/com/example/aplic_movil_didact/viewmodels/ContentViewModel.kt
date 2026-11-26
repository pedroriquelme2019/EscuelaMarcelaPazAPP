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
import com.example.aplic_movil_didact.data.remote.dto.EntregaRemoteDto
import com.example.aplic_movil_didact.data.remote.dto.FeriadoExternaDto
import com.example.aplic_movil_didact.data.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoRemotoUi(
    val entregasRemotas: List<EntregaRemoteDto> = emptyList(),
    val feriados: List<FeriadoExternaDto> = emptyList(),
    val cargandoRemoto: Boolean = false,
    val mensajeRemoto: String? = null
)

class ContentViewModel(
    app: Application,
    private val repositoryOverride: ContentRepository? = null
) : AndroidViewModel(app) {

    private val repo: ContentRepository by lazy {
        repositoryOverride ?: (app as MpazApp).repository
    }

    private val _estadoRemoto = MutableStateFlow(EstadoRemotoUi())
    val estadoRemoto: StateFlow<EstadoRemotoUi> = _estadoRemoto.asStateFlow()

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

    // ---------- Entregas (Room + Spring Boot) ----------
    fun getEntregas(): Flow<List<EntregaEntity>> = repo.getEntregas()

    fun getEntregasDeEstudiante(email: String): Flow<List<EntregaEntity>> =
        repo.getEntregasDeEstudiante(email)

    fun getEntregaDeEstudiante(email: String, asignatura: String, unidad: String, tarea: String): Flow<EntregaEntity?> =
        repo.getEntregaDeEstudiante(email, asignatura, unidad, tarea)

    /** onResultado(false) = la tarea ya estaba entregada y no se guardó de nuevo. */
    fun entregarTarea(entrega: EntregaEntity, onResultado: (Boolean) -> Unit) = viewModelScope.launch {
        onResultado(repo.insertEntrega(entrega))
    }

    fun cargarEntregasRemotas(email: String? = null) = viewModelScope.launch {
        _estadoRemoto.value = _estadoRemoto.value.copy(cargandoRemoto = true, mensajeRemoto = null)
        repo.obtenerEntregasRemotas(email)
            .onSuccess { lista ->
                _estadoRemoto.value = _estadoRemoto.value.copy(
                    entregasRemotas = lista,
                    cargandoRemoto = false
                )
            }
            .onFailure { e ->
                _estadoRemoto.value = _estadoRemoto.value.copy(
                    cargandoRemoto = false,
                    mensajeRemoto = "Microservicio no disponible: ${e.message ?: "error de red"}"
                )
            }
    }

    fun crearEntregaRemota(dto: EntregaRemoteDto) = viewModelScope.launch {
        _estadoRemoto.value = _estadoRemoto.value.copy(cargandoRemoto = true, mensajeRemoto = null)
        repo.sincronizarEntregaRemota(
            EntregaEntity(
                estudianteEmail = dto.estudianteEmail,
                estudianteNombre = dto.estudianteEmail,
                asignatura = dto.asignatura,
                unidadTitulo = "Unidad General",
                tareaTitle = dto.tareaTitulo,
                respuestaTexto = dto.observacion ?: "",
                calificacion = dto.calificacion?.toFloat(),
                observacion = dto.observacion,
                calificada = dto.calificacion != null
            )
        ).onSuccess {
            cargarEntregasRemotas()
            _estadoRemoto.value = _estadoRemoto.value.copy(mensajeRemoto = "Entrega enviada al microservicio")
        }.onFailure { e ->
            _estadoRemoto.value = _estadoRemoto.value.copy(
                cargandoRemoto = false,
                mensajeRemoto = "No se pudo enviar: ${e.message ?: "error de red"}"
            )
        }
    }

    fun eliminarEntregaRemota(id: Long) = viewModelScope.launch {
        _estadoRemoto.value = _estadoRemoto.value.copy(cargandoRemoto = true, mensajeRemoto = null)
        repo.eliminarEntregaRemota(id)
            .onSuccess {
                cargarEntregasRemotas()
                _estadoRemoto.value = _estadoRemoto.value.copy(mensajeRemoto = "Entrega eliminada del microservicio")
            }
            .onFailure { e ->
                _estadoRemoto.value = _estadoRemoto.value.copy(
                    cargandoRemoto = false,
                    mensajeRemoto = "Error al eliminar: ${e.message ?: "error de red"}"
                )
            }
    }

    // ---------- API Externa (Feriados) ----------
    fun cargarFeriadosExternos() = viewModelScope.launch {
        _estadoRemoto.value = _estadoRemoto.value.copy(cargandoRemoto = true, mensajeRemoto = null)
        repo.obtenerFeriadosExternos()
            .onSuccess { lista ->
                _estadoRemoto.value = _estadoRemoto.value.copy(
                    feriados = lista.take(5),
                    cargandoRemoto = false
                )
            }
            .onFailure { e ->
                _estadoRemoto.value = _estadoRemoto.value.copy(
                    cargandoRemoto = false,
                    mensajeRemoto = "API externa sin respuesta: ${e.message ?: "error de red"}"
                )
            }
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
