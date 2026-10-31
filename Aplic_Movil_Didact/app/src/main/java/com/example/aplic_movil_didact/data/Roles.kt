package com.example.aplic_movil_didact.data

/**
 * Roles de la app. El rol ya NO se elige en el login (punto 5):
 * se saca del dominio del correo y se verifica contra el atributo
 * "role" que tiene guardado el usuario en la BD (punto 3).
 */
object Roles {
    const val ESTUDIANTE = "Estudiante"
    const val DOCENTE = "Docente"
    const val UTP = "UTP / Administrador"

    const val DOMINIO_ESTUDIANTE = "@emp.cl"
    const val DOMINIO_DOCENTE = "@docente.cl"
    const val DOMINIO_UTP = "@utp.cl"

    /** Devuelve el rol según el dominio del correo, o null si el dominio no es del colegio. */
    fun desdeCorreo(email: String): String? {
        val correo = email.trim().lowercase()
        return when {
            correo.endsWith(DOMINIO_ESTUDIANTE) -> ESTUDIANTE
            correo.endsWith(DOMINIO_DOCENTE) -> DOCENTE
            correo.endsWith(DOMINIO_UTP) -> UTP
            else -> null
        }
    }
}
