# NASA Space Explorer

Cliente Android para la [NASA Image and Video Library](https://images.nasa.gov/). Permite buscar imágenes con paginación incremental, explorar un feed aleatorio y consultar el detalle de cada imagen. Está desarrollado con Kotlin, Jetpack Compose y Clean Architecture.

## Funcionalidades

- **Búsqueda incremental** con debounce de 400 ms y cancelación de la petición anterior.
- **Paginación** de 10 resultados por página, cargada bajo demanda al acercarse al final de la lista.
- **Feed aleatorio** cuando no hay texto de búsqueda: cada página procede de un tema distinto y el feed es ilimitado.
- **Detalle** de la imagen con título, fecha, centro de la NASA, fotógrafo y descripción.
- **Caché en memoria** con un TTL de 5 minutos para búsquedas y detalle.
- **Estados** de carga, error con reintento y resultado vacío.
- **Tema** claro y oscuro según el sistema. Interfaz en inglés.
- **Accesibilidad:** soporte para TalkBack, Switch Access, navegación por teclado y escalado de fuente al 200%.

## API

Base URL: `https://images-api.nasa.gov/`. No requiere autenticación.

| Uso | Petición |
|---|---|
| Búsqueda y feed aleatorio | `GET /search?q={query}&page={n}&page_size=10&media_type=image` |
| Detalle (si no está en caché) | `GET /search?nasa_id={id}&media_type=image` |

Las imágenes se sirven desde `images-assets.nasa.gov`.

## Requisitos

- Android Studio con soporte para AGP 9.5
- JDK 25 (Gradle lo aprovisiona automáticamente si no está instalado)
- Android SDK 37
- Dispositivo o emulador con Android 7.0 (API 24) o superior

## Compilación y ejecución

| Tarea | Comando |
|---|---|
| Instalar en dispositivo | `./gradlew installDebug` |
| Compilar debug y release | `./gradlew assembleDebug assembleRelease` |
| Tests unitarios | `./gradlew :domain:test testDebugUnitTest` |
| Tests instrumentados | `./gradlew :ui:connectedDebugAndroidTest` |
| Análisis estático | `./gradlew lintDebug` |

## Arquitectura

Clean Architecture en cuatro módulos Gradle, con MVVM en la capa de presentación. La regla de dependencias es `data → domain ← ui`.

```
               app
            /   |   \
         ui → domain ← data
```

| Módulo | Responsabilidad |
|---|---|
| `domain` | Kotlin puro, sin dependencias de Android. Contiene los modelos, `DomainError`, la interfaz `NasaImageRepository` y los casos de uso. |
| `data` | Implementación del repositorio: Retrofit, DTOs, mappers y caché. Todo es `internal` salvo el módulo de Koin. |
| `ui` | Pantallas en Compose, ViewModels y navegación type-safe. Solo depende de `domain`. |
| `app` | `Application`, `MainActivity` y composición del grafo de Koin. |

Flujo de una búsqueda:

```
SearchBar → HomeViewModel → SearchImagesUseCase → NasaImageRepositoryImpl ─┬─ SearchCache
                                                                           └─ NasaRemoteDataSource → API
```

### Presentación

Cada pantalla se compone de dos archivos:

- `XViewModel.kt` contiene:
  - el estado inmutable (`XState`), expuesto como `StateFlow` y actualizado con `update { it.copy(...) }`;
  - los eventos de navegación (`XEvent`), enviados por un `Channel` con buffer y expuestos como `Flow`, para que no se pierdan si no hay colector activo;
  - una función pública por acción de usuario.
- `XScreen.kt` contiene:
  - `XRoute`, que resuelve el ViewModel, recoge estado y eventos con conciencia del ciclo de vida y navega;
  - `XScreen`, sin estado, que se puede previsualizar y testear de forma aislada.

## Decisiones técnicas

### Cancelación de búsquedas

- **Debounce:** `HomeViewModel` aplica `debounce` de 400 ms y normaliza la consulta con `trim`.
- **Cancelación inmediata:** cada carga se ejecuta en un `Job` que se cancela en cuanto cambia el texto, sin esperar al debounce, y antes de iniciar cualquier carga nueva. En ese mismo momento la lista se vacía y se muestra el estado de carga, así que nunca se ven resultados de una consulta anterior junto al texto nuevo.
- **Propagación a la red:** las llamadas de Retrofit son `suspend` y se cancelan con la corrutina. `apiCall` relanza `CancellationException` en lugar de convertirla en error.
- **Ámbito:** todas las cargas se ejecutan en `viewModelScope`.

### Caché

| Situación | Comportamiento |
|---|---|
| Entrada válida (< 5 min) | Se devuelve sin petición de red |
| Entrada caducada o inexistente | Petición de red y almacenamiento del resultado |
| Error de red | No se almacena |

Detalles de `SearchCache`:

- **Clave:** consulta normalizada (`trim().lowercase()`) + número de página.
- **TTL:** 5 minutos medidos con `TimeSource.Monotonic`. En los tests se usa `TestTimeSource`.
- **Capacidad:** 100 entradas con política LRU (`LinkedHashMap` en orden de acceso).
- **Concurrencia:** acceso `@Synchronized`.
- **Detalle:** `findImage(id)` resuelve el detalle desde las páginas en caché.
- **Ciclo de vida:** se registra como `single` en Koin. Persiste durante el proceso y no se guarda en disco.

### Paginación y feed aleatorio

- **Disparo:** la lista observa su posición con `snapshotFlow` y solicita la página siguiente cuando quedan 5 elementos.
- **Control:** `canLoadNextPage` evita peticiones concurrentes y los ids duplicados se filtran al concatenar páginas.
- **Feed aleatorio:** `GetRandomImagesUseCase` baraja 15 temas y selecciona una página inicial aleatoria (1–20). La página `n` usa el tema `(n-1) % 15` y la página de API `inicial + (n-1) / 15`. El `Random` se inyecta para hacer el comportamiento determinista en los tests.

### Concurrencia y errores

- **Flujos:** repositorio y casos de uso exponen `Flow<Result<T>>`. El repositorio aplica `flowOn` con un dispatcher inyectable (`Dispatchers.IO` por defecto).
- **Errores:** `DomainError` es una `sealed class` con `Network`, `Server(code)`, `NotFound` y `Unknown`. `apiCall` traduce las excepciones de Retrofit y OkHttp, que no salen de `data`.

### Mapeo de datos

- **DTOs:** Gson con campos nullables y `@SerializedName`.
- **Descarte:** los resultados sin `nasa_id` se descartan.
- **HTML:** se elimina el HTML de títulos y descripciones.
- **Títulos truncados:** los títulos que la API corta con `...` se completan a partir de la descripción.
- **Imágenes:** miniatura para la lista y la variante más ancha disponible para el detalle, nunca el original.

### Inyección de dependencias

- **Módulos:** Koin con un módulo por capa (`domainModule`, `dataModule`, `uiModule`), agregados en `appModules`.
- **Declaraciones:** lambdas explícitas con argumentos nombrados.
- **Verificación:** `AppModulesTest` valida el grafo completo con `verify()`.

## Tests

### Unitarios: 92 tests, JUnit 5

Herramientas: Mockito + mockito-kotlin, kotlinx-coroutines-test, Turbine y MockWebServer.

| Módulo | Tests | Cobertura |
|---|---|---|
| `domain` | 10 | Casos de uso contra un repositorio simulado. |
| `data` | 48 | Ver detalle debajo. |
| `ui` / `app` | 34 | Ver detalle debajo. |

En `data`:

- **`SearchCacheTest`:** expiración en el límite (4:59 válida, 5:00 caducada), LRU y normalización de claves.
- **`NasaImageRepositoryImplTest`:** cuenta peticiones reales contra MockWebServer para comprobar cache hit, expiración, que los errores no se cachean y el detalle desde caché.
- **Otros:** `apiCall`, incluida la propagación de la cancelación, y los mappers.

En `ui` / `app`:

- **`HomeViewModelTest`:** cancelación, resultados ocultos durante el debounce, paginación, feed aleatorio, errores, reintento y navegación.
- **Eventos:** en los tres ViewModels, un evento enviado sin colector llega al empezar a colectar.
- **Resto de pantallas y utilidades:** `DetailViewModel` y `SplashViewModel`, el formateo de fechas y el grafo de Koin.

### Instrumentados: 27 tests, JUnit 4

Usan JUnit 4 porque lo requieren `createComposeRule` y `AndroidJUnitRunner`.

| Clase | Tests | Cobertura |
|---|---|---|
| `TalkBackTest` | 12 | Semántica, encabezados, regiones vivas y orden de lectura |
| `FontScalingTest` | 6 | Fuente al 200%: sin texto truncado y áreas táctiles de al menos 48 dp |
| `SwitchAccessTest` | 9 | Foco, nombres accesibles y activación por teclado |

## Integración continua

GitHub Actions (`.github/workflows/`):

| Workflow | Descripción |
|---|---|
| `build.yml` | Compila debug y release (con R8) y ejecuta Android Lint |
| `tests.yml` | Ejecuta los tests unitarios y publica los informes como artefacto |

- **Disparadores:** push a `main`, `develop`, `feature/**`, `fix/**` y `release/**`, y pull requests a `main` y `develop`.
- **Entorno:** JDK 25 (Temurin) con caché de Gradle.
- **Concurrencia:** cancela las ejecuciones previas de la misma rama.
- **Seguridad:**
  - Dependabot: alertas y actualizaciones semanales de Gradle y Actions.
  - CodeQL (default setup de GitHub, Kotlin y workflows) y secret scanning.
  - Política de reporte de vulnerabilidades en [`SECURITY.md`](SECURITY.md).

## Accesibilidad

- **TalkBack:**
  - Cada resultado se anuncia como un único elemento con acción.
  - Fechas verbalizadas.
  - Encabezados y títulos de pantalla.
  - Regiones vivas para carga, error y vacío.
- **Escalado de fuente:**
  - Sin truncado de texto hasta el 200%.
  - Contenido desplazable y adaptado al teclado (`imePadding`).
  - Áreas táctiles de al menos 48 dp.
- **Switch Access y teclado:**
  - Una parada por elemento, con nombre y rol.
  - Indicador de foco visible.
  - Toda la funcionalidad es operable con Tab y Enter.

## Stack

| Área | Tecnología |
|---|---|
| Lenguaje y build | Kotlin 2.4.21 · Gradle 9.9.1 · AGP 9.5.1 · version catalog |
| UI | Compose BOM 2026.09.00 · Material 3 · Navigation Compose 2.10.2 · Lifecycle 2.11.0 |
| DI y asincronía | Koin 4.2.2 · Coroutines 1.11.0 |
| Red e imágenes | Retrofit 3.0.0 · OkHttp 5.5.1 · Gson · Coil 3.6.3 |
| Tests | JUnit 5.14.4 · Mockito 5.24.0 · mockito-kotlin 6.4.0 · Turbine 1.2.1 · MockWebServer · Compose UI Test · Espresso 3.7.0 |
| SDK | minSdk 24 · targetSdk 37 · compileSdk 37 |

**Diseño:**

- Material 3 con tema propio.
- Tipografía Space Grotesk.
- Radios de 4 dp como máximo.
- Iconografía vectorial propia.

Las convenciones de cada módulo están documentadas en su `AGENTS.md`. El índice general está en [`app/AGENTS.md`](app/AGENTS.md).

## Flujo de trabajo

Gitflow:

- **`main`:** releases.
- **`develop`:** integración.
- **`feature/*` y `fix/*`:** trabajo en curso, integrado en `develop` con `merge --no-ff`.

## Licencias

- **Contenido:** el contenido de la NASA es, en general, de dominio público. Consulta sus [directrices de uso](https://www.nasa.gov/nasa-brand-center/images-and-media/).
- **Logo:** el logotipo de la NASA es una marca registrada. Su uso no implica respaldo ni afiliación.
- **Fuente:** Space Grotesk se distribuye bajo SIL Open Font License 1.1 (`licenses/SpaceGrotesk-OFL.txt`).
