package com.example.aplic_movil_didact.viewmodels

import android.app.Application
import com.example.aplic_movil_didact.data.entity.EntregaEntity
import com.example.aplic_movil_didact.data.remote.dto.FeriadoExternaDto
import com.example.aplic_movil_didact.data.repository.ContentRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application
    private lateinit var repository: ContentRepository
    private lateinit var viewModel: ContentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        app = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        viewModel = ContentViewModel(app = app, repositoryOverride = repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entregarTarea invoca callback con true cuando repositorio guarda correctamente`() = runTest(testDispatcher) {
        val entrega = EntregaEntity(
            estudianteEmail = "carlos.lopez@emp.cl",
            estudianteNombre = "Carlos López",
            asignatura = "Matemática",
            unidadTitulo = "Unidad 1",
            tareaTitle = "Tarea 1",
            respuestaTexto = "Resuelto"
        )
        coEvery {
            repository.insertEntrega(match {
                it.estudianteEmail == entrega.estudianteEmail &&
                    it.estudianteNombre == entrega.estudianteNombre &&
                    it.asignatura == entrega.asignatura &&
                    it.unidadTitulo == entrega.unidadTitulo &&
                    it.tareaTitle == entrega.tareaTitle &&
                    it.respuestaTexto == entrega.respuestaTexto &&
                    it.nombreArchivo == entrega.nombreArchivo
            })
        } returns true

        var resultadoCallback = false
        viewModel.entregarTarea(
            estudianteEmail = entrega.estudianteEmail,
            estudianteNombre = entrega.estudianteNombre,
            asignatura = entrega.asignatura,
            unidadTitulo = entrega.unidadTitulo,
            tareaTitle = entrega.tareaTitle,
            respuestaTexto = entrega.respuestaTexto,
            nombreArchivo = entrega.nombreArchivo
        ) { ok -> resultadoCallback = ok }
        advanceUntilIdle()

        resultadoCallback.shouldBeTrue()
    }

    @Test
    fun `cargarFeriadosExternos actualiza estadoRemoto con los feriados obtenidos`() = runTest {
        val feriados = listOf(
            FeriadoExternaDto(date = "2026-12-25", title = "Navidad", type = "Religioso", inalienable = true)
        )
        coEvery { repository.obtenerFeriadosExternos() } returns Result.success(feriados)

        viewModel.cargarFeriadosExternos()
        advanceUntilIdle()

        viewModel.estadoRemoto.value.feriados.size shouldBe 1
        viewModel.estadoRemoto.value.feriados.first().title shouldBe "Navidad"
    }
}
