package com.example.aplic_movil_didact.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Main : Screen("main_screen/{email}/{nombre}/{rol}") {
        fun withSesion(email: String, nombre: String, rol: String) =
            "main_screen/${Uri.encode(email)}/${Uri.encode(nombre)}/${Uri.encode(rol)}"
        const val ARG_EMAIL = "email"
        const val ARG_NOMBRE = "nombre"
        const val ARG_ROL = "rol"
    }
    /** Pestañas principales (se deslizan con un HorizontalPager). */
    data object Tabs : Screen("tabs_screen")
    data object Subject : Screen("subject_screen/{asignaturaId}/{asignaturaNombre}") {
        fun withAsignatura(id: Int, nombre: String) = "subject_screen/$id/${Uri.encode(nombre)}"
        const val ARG_ID = "asignaturaId"
        const val ARG_NOMBRE = "asignaturaNombre"
    }
    data object Task : Screen("task_screen/{tareaTitle}/{asignatura}/{unidadTitulo}") {
        fun withContext(titulo: String, asignatura: String, unidad: String) =
            "task_screen/${Uri.encode(titulo)}/${Uri.encode(asignatura)}/${Uri.encode(unidad)}"
        const val ARG_TITLE = "tareaTitle"
        const val ARG_ASIGNATURA = "asignatura"
        const val ARG_UNIDAD = "unidadTitulo"
    }
    /** Revisión de una tarea puntual (docente/UTP): quiénes la hicieron y sus notas. */
    data object Revisar : Screen("revisar_screen/{tareaTitle}/{asignatura}/{unidadTitulo}") {
        fun withContext(titulo: String, asignatura: String, unidad: String) =
            "revisar_screen/${Uri.encode(titulo)}/${Uri.encode(asignatura)}/${Uri.encode(unidad)}"
        const val ARG_TITLE = "tareaTitle"
        const val ARG_ASIGNATURA = "asignatura"
        const val ARG_UNIDAD = "unidadTitulo"
    }
}
