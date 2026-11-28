# Escuela Marcela Paz APP (`Aplic_Movil_Didact`)

Aplicación móvil educativa desarrollada en **Kotlin** y **Jetpack Compose** bajo arquitectura **MVVM** para la asignatura **DSY1105**, orientada a la gestión académica de asignaturas, unidades, entregas de tareas, calificaciones e informes pedagógicos con control de acceso según rol (**Estudiante**, **Docente** y **UTP**).

---

## 1. Arquitectura y Stack Tecnológico
* **Lenguaje y UI:** Kotlin + Jetpack Compose (Material 3) + Navigation Compose.
* **Arquitectura:** MVVM (`ui/screens` → `viewmodels` → `data/repository` → `data/local` + `data/remote`).
* **Persistencia Local:** Room (`AppDatabase`, DAOs y Entities reactivas mediante `Flow`).
* **Consumo de APIs REST:** Retrofit 2 + Gson + OkHttp Logging Interceptor.
* **Pruebas Unitarias:** JUnit 4, MockK, Kotest Assertions y Kotlinx Coroutines Test (`app/src/test`).
* **Firma y Distribución:** APK de versión `release` firmado con Keystore `.jks`.

---

## 2. Integración de Servicios Remotos
* **Microservicio Propio (Spring Boot CRUD):**
    * **Base URL (emulador Android):** `http://10.0.2.2:8080/`
    * **Interfaz:** `EntregaApiService` (`GET /api/entregas`, `POST /api/entregas`, `PUT /api/entregas/{id}`, `DELETE /api/entregas/{id}`).
    * **Uso:** Sincroniza entregas de estudiantes y operaciones CRUD sobre entregas, manteniendo respaldo y validación en Room.
* **API Externa Pública (Feriados de Chile):**
    * **Base URL:** `https://api.boostr.cl/`
    * **Interfaz:** `FeriadosApiService` (`GET /api/v1/feriados`).
    * **Uso:** Consulta feriados externos para mostrarlos en `HomeScreen` con manejo seguro de errores/red.

---

## 3. Roles y Funcionalidades Principales
* **Estudiante:** Visualiza asignaturas y unidades habilitadas, envía respuestas de tareas con validación para evitar entregas duplicadas y revisa sus calificaciones y observaciones.
* **Docente:** Revisa entregas recibidas, registra calificaciones y retroalimentación, y da respuesta a solicitudes de informes pedagógicos.
* **UTP:** Habilita o deshabilita asignaturas y unidades, revisa el avance académico y solicita informes pedagógicos.

---

## 4. Estructura General del Proyecto
* `Aplic_Movil_Didact/app/src/main/java/com/example/aplic_movil_didact/`
    * `data/local/`: Base de datos Room, entidades (`UserEntity`, etc.) y DAOs.
    * `data/remote/`: `RetrofitClient`, APIs (`EntregaApiService`, `FeriadosApiService`) y DTOs.
    * `data/repository/`: `ContentRepository` (coordina Room y servicios remotos).
    * `viewmodels/`: `ContentViewModel` y estados de UI.
    * `ui/screens/`: Pantallas Compose (incluyendo `HomeScreen` y flujos por rol).
* `Aplic_Movil_Didact/app/src/test/`: Pruebas unitarias (`ContentRepositoryTest`, `ContentViewModelTest`, etc.).

---

## 5. Requisitos Previos
* **Android Studio** actualizado con SDK de Android y emulador configurado (o dispositivo físico con depuración USB).
* **JDK** compatible con el proyecto (incluido habitualmente con Android Studio).
* *(Opcional)* Microservicio Spring Boot ejecutándose en el puerto `8080` para pruebas de integración CRUD completas; la aplicación maneja fallos de conexión de forma controlada si el servidor está apagado o sin red.

---

## 6. Cómo Ejecutar el Proyecto

### Opción A: Desde Android Studio
1. Abre **Android Studio** y selecciona **File > Open...**
2. Navega hasta la carpeta `Aplic_Movil_Didact` dentro de tu repositorio y haz clic en **OK**.
3. Espera a que termine la sincronización de Gradle (**Sync**).
4. Selecciona un emulador o dispositivo en la barra superior y haz clic en **Run (`▶`)** para instalar y ejecutar la app.

### Opción B: Desde Terminal / Consola
Ubícate en la carpeta `Aplic_Movil_Didact`:

    cd Aplic_Movil_Didact

* **En Windows (PowerShell o CMD):**

  .\gradlew.bat assembleDebug

* **En macOS / Linux:**

  ./gradlew assembleDebug

---

## 7. Cómo Ejecutar las Pruebas Unitarias

* **Desde Android Studio:**
    1. En el panel izquierdo (vista **Android**), despliega `app > kotlin+java`.
    2. Haz clic derecho sobre `com.example.aplic_movil_didact (test)` y elige **Run 'All Tests'**.
* **Desde Terminal:**

  # Windows
  .\gradlew.bat testDebugUnitTest

  # macOS / Linux
  ./gradlew testDebugUnitTest

---

## 8. Generación de APK Firmado (`release`)
1. En Android Studio, ve a **Build > Generate Signed App Bundle / APK...**
2. Elige **APK** y haz clic en **Next**.
3. Selecciona o crea tu archivo keystore `.jks` (`escuela_marcela_paz.jks`), ingresa las credenciales correspondientes y haz clic en **Next**.
4. Selecciona el Build Variant **`release`** y haz clic en **Create**.
5. El APK firmado quedará generado en `app/release/app-release.apk`.

> **Nota de seguridad:** No subas archivos `.jks`, contraseñas ni carpetas de compilación/caché (`build/`, `.gradle/`, `.kotlin/`) al repositorio público.