package com.example.aplic_movil_didact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplic_movil_didact.data.Roles
import com.example.aplic_movil_didact.navigation.Screen
import com.example.aplic_movil_didact.ui.screens.*
import com.example.aplic_movil_didact.ui.theme.Aplic_Movil_DidactTheme
import com.example.aplic_movil_didact.viewmodels.ContentViewModel
import com.example.aplic_movil_didact.viewmodels.LoginViewModel
import com.example.aplic_movil_didact.viewmodels.SesionUsuario
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** Pestañas de abajo. Cada rol ve solo las suyas (punto 6). */
enum class Pestana(val label: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Filled.Home),
    MIS_NOTAS("Mis notas", Icons.Filled.Grade),
    AVANCES("Avances", Icons.Filled.Assessment),
    INFORMES("Informes", Icons.AutoMirrored.Filled.Assignment),
    AJUSTES("Ajustes", Icons.Filled.Settings)
}

fun pestanasPara(rol: String): List<Pestana> = when (rol) {
    Roles.DOCENTE -> listOf(Pestana.INICIO, Pestana.AVANCES, Pestana.INFORMES, Pestana.AJUSTES)
    Roles.UTP -> listOf(Pestana.INICIO, Pestana.INFORMES, Pestana.AJUSTES)
    else -> listOf(Pestana.INICIO, Pestana.MIS_NOTAS, Pestana.AJUSTES) // Estudiante
}

private val slideSpec = tween<IntOffset>(320)
private val fadeSpec = tween<Float>(320)

private val enterForward = slideInHorizontally(slideSpec) { it } + fadeIn(fadeSpec)
private val exitForward  = slideOutHorizontally(slideSpec) { -it / 3 } + fadeOut(fadeSpec)
private val enterBack    = slideInHorizontally(slideSpec) { -it / 3 } + fadeIn(fadeSpec)
private val exitBack     = slideOutHorizontally(slideSpec) { it } + fadeOut(fadeSpec)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Aplic_Movil_DidactTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(
        navController = rootNavController,
        startDestination = Screen.Login.route,
        enterTransition = { enterForward },
        exitTransition = { exitForward },
        popEnterTransition = { enterBack },
        popExitTransition = { exitBack }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                loginViewModel = loginViewModel,
                onLoginSuccess = { sesion ->
                    rootNavController.navigate(Screen.Main.withSesion(sesion.email, sesion.nombre, sesion.rol)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Main.route,
            arguments = listOf(
                navArgument(Screen.Main.ARG_EMAIL) { type = NavType.StringType },
                navArgument(Screen.Main.ARG_NOMBRE) { type = NavType.StringType },
                navArgument(Screen.Main.ARG_ROL) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val sesion = SesionUsuario(
                email = args?.getString(Screen.Main.ARG_EMAIL) ?: "",
                nombre = args?.getString(Screen.Main.ARG_NOMBRE) ?: "",
                rol = args?.getString(Screen.Main.ARG_ROL) ?: Roles.ESTUDIANTE
            )
            MainScreen(
                sesion = sesion,
                onCerrarSesion = {
                    // Punto 4: cerrar sesión y volver al login para entrar con otro rol
                    loginViewModel.cerrarSesion()
                    rootNavController.navigate(Screen.Login.route) {
                        popUpTo(rootNavController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(sesion: SesionUsuario, onCerrarSesion: () -> Unit) {
    val navController = rememberNavController()
    val contentViewModel: ContentViewModel = viewModel()
    val pestanas = remember(sesion.rol) { pestanasPara(sesion.rol) }
    val pagerState = rememberPagerState(pageCount = { pestanas.size })
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val enPestanas = backStackEntry?.destination?.route.let { it == null || it == Screen.Tabs.route }

    Scaffold(
        bottomBar = {
            NavigationBar {
                pestanas.forEachIndexed { index, pestana ->
                    NavigationBarItem(
                        icon = { Icon(pestana.icon, contentDescription = pestana.label) },
                        label = { Text(pestana.label) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            if (!enPestanas) {
                                navController.popBackStack(Screen.Tabs.route, inclusive = false)
                            }
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tabs.route,
            modifier = Modifier.padding(innerPadding),
            // Punto 13: al entrar a una asignatura/tarea la pantalla se desliza
            enterTransition = { enterForward },
            exitTransition = { exitForward },
            popEnterTransition = { enterBack },
            popExitTransition = { exitBack }
        ) {
            composable(Screen.Tabs.route) {
                PestanasDeslizables(
                    pagerState = pagerState,
                    pestanas = pestanas,
                    sesion = sesion,
                    contentViewModel = contentViewModel,
                    onSubjectClick = { id, nombre ->
                        navController.navigate(Screen.Subject.withAsignatura(id, nombre))
                    },
                    onCerrarSesion = onCerrarSesion
                )
            }
            composable(
                route = Screen.Subject.route,
                arguments = listOf(
                    navArgument(Screen.Subject.ARG_ID) { type = NavType.IntType },
                    navArgument(Screen.Subject.ARG_NOMBRE) { type = NavType.StringType }
                )
            ) { entry ->
                val asignaturaId = entry.arguments?.getInt(Screen.Subject.ARG_ID) ?: 0
                val nombre = entry.arguments?.getString(Screen.Subject.ARG_NOMBRE) ?: ""
                SubjectScreen(
                    asignaturaId = asignaturaId,
                    asignatura = nombre,
                    sesion = sesion,
                    contentViewModel = contentViewModel,
                    onBack = { navController.popBackStack() },
                    onIniciarTarea = { titulo, asig, unidad ->
                        navController.navigate(Screen.Task.withContext(titulo, asig, unidad))
                    },
                    onRevisarTarea = { titulo, asig, unidad ->
                        navController.navigate(Screen.Revisar.withContext(titulo, asig, unidad))
                    }
                )
            }
            composable(
                route = Screen.Task.route,
                arguments = listOf(
                    navArgument(Screen.Task.ARG_TITLE) { type = NavType.StringType },
                    navArgument(Screen.Task.ARG_ASIGNATURA) { type = NavType.StringType },
                    navArgument(Screen.Task.ARG_UNIDAD) { type = NavType.StringType }
                )
            ) { entry ->
                TaskScreen(
                    tareaTitle = entry.arguments?.getString(Screen.Task.ARG_TITLE) ?: "",
                    asignatura = entry.arguments?.getString(Screen.Task.ARG_ASIGNATURA) ?: "",
                    unidadTitulo = entry.arguments?.getString(Screen.Task.ARG_UNIDAD) ?: "",
                    sesion = sesion,
                    contentViewModel = contentViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Revisar.route,
                arguments = listOf(
                    navArgument(Screen.Revisar.ARG_TITLE) { type = NavType.StringType },
                    navArgument(Screen.Revisar.ARG_ASIGNATURA) { type = NavType.StringType },
                    navArgument(Screen.Revisar.ARG_UNIDAD) { type = NavType.StringType }
                )
            ) { entry ->
                MonitoreoScreen(
                    contentViewModel = contentViewModel,
                    filtro = FiltroTarea(
                        tareaTitle = entry.arguments?.getString(Screen.Revisar.ARG_TITLE) ?: "",
                        asignatura = entry.arguments?.getString(Screen.Revisar.ARG_ASIGNATURA) ?: "",
                        unidadTitulo = entry.arguments?.getString(Screen.Revisar.ARG_UNIDAD) ?: ""
                    ),
                    puedeCalificar = sesion.esDocente,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Punto 13: las pestañas principales se pueden cambiar deslizando el dedo
 * (izquierda/derecha). Mientras se desliza, la página que sale se achica y
 * se desvanece un poco.
 */
@Composable
private fun PestanasDeslizables(
    pagerState: PagerState,
    pestanas: List<Pestana>,
    sesion: SesionUsuario,
    contentViewModel: ContentViewModel,
    onSubjectClick: (Int, String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        .absoluteValue
                        .coerceIn(0f, 1f)
                    val escala = 1f - 0.08f * offset
                    scaleX = escala
                    scaleY = escala
                    alpha = 1f - 0.5f * offset
                }
        ) {
            when (pestanas[page]) {
                Pestana.INICIO -> HomeScreen(
                    contentViewModel = contentViewModel,
                    sesion = sesion,
                    onSubjectClick = onSubjectClick
                )
                Pestana.MIS_NOTAS -> MisNotasScreen(
                    sesion = sesion,
                    contentViewModel = contentViewModel
                )
                Pestana.AVANCES -> MonitoreoScreen(
                    contentViewModel = contentViewModel,
                    puedeCalificar = sesion.esDocente
                )
                Pestana.INFORMES -> InformesScreen(
                    sesion = sesion,
                    contentViewModel = contentViewModel
                )
                Pestana.AJUSTES -> SettingsScreen(
                    sesion = sesion,
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
}
