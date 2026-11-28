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
import com.example.aplic_movil_didact.utils.Notas
import com.example.aplic_movil_didact.utils.formatoFecha
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

class ContentViewModel @JvmOverloads constructor(
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

    /** onResultado(false) = la tarea ya estaba entregada y no se guard? de nuevo. */
    fun entregarTarea(
        estudianteEmail: String,
        estudianteNombre: String,
        asignatura: String,
        unidadTitulo: String,
        tareaTitle: String,
        respuestaTexto: String,
        nombreArchivo: String?,
        onResultado: (Boolean) -> Unit
    ) = viewModelScope.launch {
        val entrega = EntregaEntity(
            estudianteEmail = estudianteEmail,
            estudianteNombre = estudianteNombre,
            asignatura = asignatura,
            unidadTitulo = unidadTitulo,
            tareaTitle = tareaTitle,
            respuestaTexto = respuestaTexto,
            nombreArchivo = nombreArchivo
        )
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
                    feriados = lista,
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

    fun generarBorradorInforme(
        informe: InformeEntity,
        todasLasEntregas: List<EntregaEntity>,
        totalEstudiantes: Int,
        docente: String,
        cursoCompleto: String
    ): String {
        val entregas = if (informe.asignatura == cursoCompleto) todasLasEntregas
        else todasLasEntregas.filter { it.asignatura == informe.asignatura }

        val notas = entregas.mapNotNull { it.calificacion }
        val promedio = Notas.promedio(notas)
        val bajo4 = notas.count { it < Notas.APROBACION }
        val pendientes = entregas.count { !it.calificada }
        val porTarea = entregas
            .groupBy { Triple(it.asignatura, it.unidadTitulo, it.tareaTitle) }
            .entries
            .sortedBy { "${it.key.first}|${it.key.second}|${it.key.third}" }
            .joinToString("\n") { (clave, lista) ->
                val prom = Notas.promedio(lista.mapNotNull { it.calificacion })
                val prefijo = if (informe.asignatura == cursoCompleto) "${clave.first} / " else ""
                "- $prefijo${clave.second} / ${clave.third}: ${lista.size} de $totalEstudiantes entregaron" +
                    (prom?.let { ", promedio ${Notas.formato(it)}" } ?: ", sin notas disponibles")
            }

        return buildString {
            appendLine("INFORME PEDAGOGICO - ${informe.asignatura}")
            appendLine("Docente: $docente")
            appendLine("Fecha: ${formatoFecha(System.currentTimeMillis())}")
            appendLine()
            appendLine("1. Resumen de entregas")
            appendLine("- Entregas recibidas: ${entregas.size}")
            appendLine("- Calificadas: ${notas.size} (por revisar: $pendientes)")
            appendLine("- Promedio: ${promedio?.let { Notas.formato(it) } ?: "sin notas disponibles"}")
            appendLine("- Notas bajo 4.0: $bajo4")
            appendLine()
            appendLine("2. Detalle por tarea")
            appendLine(if (porTarea.isBlank()) "- Sin entregas registradas" else porTarea)
            appendLine()
            appendLine("3. Observaciones del docente")
            appendLine("- ")
            appendLine()
            appendLine("4. Estudiantes que necesitan apoyo")
            append("- ")
        }
    }

    fun solicitarInforme(asignatura: String, indicaciones: String, solicitadoPor: String) = viewModelScope.launch {
        repo.solicitarInforme(asignatura, indicaciones, solicitadoPor)
    }

    fun responderInforme(informe: InformeEntity, contenido: String, respondidoPor: String) = viewModelScope.launch {
        repo.responderInforme(informe, contenido, respondidoPor)
    }
}
