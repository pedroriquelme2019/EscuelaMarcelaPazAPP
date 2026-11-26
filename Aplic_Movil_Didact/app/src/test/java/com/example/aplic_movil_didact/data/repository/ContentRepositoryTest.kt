package com.example.aplic_movil_didact.data.repository

import com.example.aplic_movil_didact.data.Roles
import com.example.aplic_movil_didact.data.dao.AsignaturaDao
import com.example.aplic_movil_didact.data.dao.EntregaDao
import com.example.aplic_movil_didact.data.dao.InformeDao
import com.example.aplic_movil_didact.data.dao.UnidadDao
import com.example.aplic_movil_didact.data.entity.AsignaturaEntity
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.entity.InformeEntity
import com.example.aplic_movil_didact.data.local.dao.UserDao
import com.example.aplic_movil_didact.data.local.entity.UserEntity
import com.example.aplic_movil_didact.data.remote.api.EntregaApiService
import com.example.aplic_movil_didact.data.remote.api.FeriadosApiService
import com.example.aplic_movil_didact.data.remote.dto.EntregaRemoteDto
import com.example.aplic_movil_didact.data.remote.dto.FeriadoExternaDto
import com.example.aplic_movil_didact.data.remote.dto.FeriadosResponseDto
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ContentRepositoryTest {

    private lateinit var asignaturaDao: AsignaturaDao
    private lateinit var unidadDao: UnidadDao
    private lateinit var entregaDao: EntregaDao
    private lateinit var informeDao: InformeDao
    private lateinit var userDao: UserDao
    private lateinit var entregaApi: EntregaApiService
    private lateinit var feriadosApi: FeriadosApiService
    private lateinit var repository: ContentRepository

    @Before
    fun setup() {
        asignaturaDao = mockk(relaxed = true)
        unidadDao = mockk(relaxed = true)
        entregaDao = mockk(relaxed = true)
        informeDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        entregaApi = mockk(relaxed = true)
        feriadosApi = mockk(relaxed = true)

        repository = ContentRepository(
            asignaturaDao = asignaturaDao,
            unidadDao = unidadDao,
            entregaDao = entregaDao,
            informeDao = informeDao,
            userDao = userDao,
            entregaApi = entregaApi,
            feriadosApi = feriadosApi
        )
    }

    @Test
    fun `getAsignaturas filtra habilitadas cuando soloHabilitadas es true`() = runTest {
        val lista = listOf(
            AsignaturaEntity(id = 1, nombre = "Matemática", descripcion = "Base", habilitada = true)
        )
        every { asignaturaDao.getHabilitadas() } returns flowOf(lista)

        val resultado = repository.getAsignaturas(soloHabilitadas = true).first()

        resultado.size shouldBe 1
        resultado.first().nombre shouldBe "Matemática"
    }

    @Test
    fun `toggleAsignatura invierte el estado habilitada en Room`() = runTest {
        val original = AsignaturaEntity(id = 2, nombre = "Historia", descripcion = "Chile", habilitada = true)

        repository.toggleAsignatura(original)

        coVerify(exactly = 1) {
            asignaturaDao.update(original.copy(habilitada = false))
        }
    }

    @Test
    fun `insertEntrega retorna false si la tarea ya fue entregada por el estudiante`() = runTest {
        val entrega = EntregaEntity(
            estudianteEmail = "ana.garcia@emp.cl",
            estudianteNombre = "Ana García",
            asignatura = "Matemática",
            unidadTitulo = "Unidad 1",
            tareaTitle = "Guía 1",
            respuestaTexto = "Respuesta"
        )
        coEvery {
            entregaDao.contarEntregas("ana.garcia@emp.cl", "Matemática", "Unidad 1", "Guía 1")
        } returns 1

        val resultado = repository.insertEntrega(entrega)

        resultado.shouldBeFalse()
        coVerify(exactly = 0) { entregaDao.insert(any()) }
    }

    @Test
    fun `insertEntrega guarda en Room y sincroniza con microservicio cuando es nueva`() = runTest {
        val entrega = EntregaEntity(
            estudianteEmail = "ana.garcia@emp.cl",
            estudianteNombre = "Ana García",
            asignatura = "Lenguaje y Comunicación",
            unidadTitulo = "Unidad 1",
            tareaTitle = "Ensayo",
            respuestaTexto = "Mi texto"
        )
        coEvery {
            entregaDao.contarEntregas("ana.garcia@emp.cl", "Lenguaje y Comunicación", "Unidad 1", "Ensayo")
        } returns 0
        coEvery { entregaDao.insert(entrega) } returns 1L
        coEvery { entregaApi.crearEntrega(any()) } returns EntregaRemoteDto(
            id = 10L,
            estudianteEmail = entrega.estudianteEmail,
            asignatura = entrega.asignatura,
            tareaTitulo = entrega.tareaTitle
        )

        val resultado = repository.insertEntrega(entrega)

        resultado.shouldBeTrue()
        coVerify(exactly = 1) { entregaDao.insert(entrega) }
        coVerify(exactly = 1) { entregaApi.crearEntrega(any()) }
    }

    @Test
    fun `calificarEntrega guarda nota y marca calificada en true`() = runTest {
        val entrega = EntregaEntity(
            id = 5,
            estudianteEmail = "carlos.lopez@emp.cl",
            estudianteNombre = "Carlos López",
            asignatura = "Matemática",
            unidadTitulo = "Unidad 1",
            tareaTitle = "Prueba 1",
            respuestaTexto = "Desarrollo"
        )

        repository.calificarEntrega(entrega, calificacion = 6.5f, observacion = "Muy buen trabajo")

        coVerify(exactly = 1) {
            entregaDao.update(
                entrega.copy(
                    calificacion = 6.5f,
                    observacion = "Muy buen trabajo",
                    calificada = true
                )
            )
        }
    }

    @Test
    fun `obtenerFeriadosExternos retorna lista desde API externa`() = runTest {
        val feriadosMock = listOf(
            FeriadoExternaDto(date = "2026-09-18", title = "Fiestas Patrias", type = "Civil", inalienable = true)
        )
        coEvery { feriadosApi.getFeriados() } returns FeriadosResponseDto(status = "success", data = feriadosMock)

        val resultado = repository.obtenerFeriadosExternos()

        resultado.isSuccess.shouldBeTrue()
        resultado.getOrNull()?.size shouldBe 1
        resultado.getOrNull()?.first()?.title shouldBe "Fiestas Patrias"
    }

    @Test
    fun `solicitarInforme guarda un nuevo InformeEntity en Room`() = runTest {
        repository.solicitarInforme("Ciencias", "Revisar avance", "utp@escuela.cl")

        coVerify(exactly = 1) {
            informeDao.insert(
                match<InformeEntity> {
                    it.asignatura == "Ciencias" &&
                            it.indicaciones == "Revisar avance" &&
                            it.solicitadoPor == "utp@escuela.cl"
                }
            )
        }
    }

    @Test
    fun `getEstudiantes consulta usuarios con rol ESTUDIANTE`() = runTest {
        val alumnos = listOf(
            UserEntity(email = "alu@emp.cl", name = "Alumno", password = "123", role = Roles.ESTUDIANTE)
        )
        every { userDao.getUsersByRole(Roles.ESTUDIANTE) } returns flowOf(alumnos)

        val resultado = repository.getEstudiantes().first()

        resultado.size shouldBe 1
        resultado.first().email shouldBe "alu@emp.cl"
    }
}