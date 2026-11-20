# AGENTS.md · Módulo `domain`

Guía general del proyecto: [`../app/AGENTS.md`](../app/AGENTS.md).

## Qué es

El centro de la arquitectura: las reglas de negocio en **Kotlin puro** (módulo JVM, sin Android). No depende de ningún otro módulo; `data` y `ui` dependen de él.

Dependencias permitidas: solo `kotlinx-coroutines-core` (expuesta con `api`, porque `Flow` aparece en las firmas públicas) y `koin-core` para su módulo de DI. **Nunca** añadas Android, Retrofit, Gson, Compose ni nada de `data` o `ui`.

## Estructura

```
model/        NasaImage, SearchPage, RandomFeed       (data class inmutables)
error/        DomainError                             (errores que entiende la UI)
repository/   NasaImageRepository                     (interfaz; la implementa data)
usecase/      SearchImagesUseCase, GetImageUseCase, GetRandomImagesUseCase
di/           domainModule
```

## Piezas clave

- **`NasaImageRepository`:** el contrato con `data`. Todo devuelve `Flow<Result<T>>`:
  - `search(query, page)`: una página de resultados.
  - `getImage(id)`: una imagen.

  Los errores llegan dentro del `Result` como `DomainError`, nunca como excepciones lanzadas.
- **`DomainError`:** `sealed class` que hereda de `Exception`, con `Network`, `Server(code)`, `NotFound` y `Unknown`.
  - Los casos sin datos son `class` (no `object` ni `data object`), porque una excepción debe crearse nueva cada vez para tener su propia traza: `DomainError.Network()`.
  - Se comparan **por tipo** (`is DomainError.Network`), no con `==`.
- **`SearchPage`:** `images` + `hasNextPage`. La paginación es de 10 en 10 (el tamaño lo fija `data`).
- **Casos de uso:**
  - `SearchImagesUseCase` quita los espacios de la búsqueda y empieza en `FIRST_PAGE = 1`.
  - `GetImageUseCase` pide una imagen por id.
  - `GetRandomImagesUseCase` genera el feed aleatorio:
    - `newFeed()` baraja `TOPICS` y elige una página inicial entre 1 y `MAX_FIRST_PAGE`.
    - `invoke(feed, page)` usa un tema distinto en cada página y pasa a la siguiente página de la API al completar la vuelta.
    - Marca siempre `hasNextPage = true`, porque el feed no se acaba.
    - El `Random` se inyecta para poder testearlo.

## Reglas

- **Un caso de uso = una clase pequeña** con `operator fun invoke(...)`. Sin estado, salvo lo que reciba por constructor.
- **Modelos:** `data class` inmutables, sin campos nulos salvo que el dato sea opcional de verdad (como `photographer` o las URLs de imagen). Los nulos de la API se resuelven en los mappers de `data`.
- **Sin hilos:** nada de `Dispatchers` ni decisiones de hilos aquí. `domain` no sabe en qué hilo corre; eso lo decide `data`.
- **Sin caché:** `domain` no conoce la caché. Es un detalle de `data`.
- **DI:** registra cada caso de uso en `di/DomainModule.kt` con lambda y argumentos con nombre: `factory { X(repository = get()) }`. Los valores por defecto (como `Random.Default`) no se pasan.

## Tests (`src/test`)

- **JUnit 5 (Jupiter) + `kotlinx-coroutines-test`** (`runTest`). Los Flow se leen con `.first()`.
- **Mockito con `mockito-kotlin`:**
  - Se simula el repositorio con `private val repository: NasaImageRepository = mock()`.
  - Las respuestas se configuran con `whenever(repository.search(any(), any())).thenReturn(flowOf(...))`.
  - Las llamadas se comprueban con `verify(repository).search(query = ..., page = ...)`, o con `inOrder` si importa el orden.
  - No escribas fakes a mano.
- **Agente de Mockito:** se carga con `-javaagent` desde la configuración `mockitoAgent` de `build.gradle.kts`, de forma perezosa y compatible con la caché de configuración. Si añades tests en otro módulo con Mockito, replica esa configuración.
- **Aleatoriedad:** se prueba con `Random(semilla)` fija.
- **Nombres:** frases entre backticks: ``fun `trims the query before searching`()``.

Ejecutar:

```bash
./gradlew :domain:test
```
