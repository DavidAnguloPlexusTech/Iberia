# AGENTS.md · Módulo `data`

Guía general del proyecto: [`../app/AGENTS.md`](../app/AGENTS.md).

## Qué es

La implementación de `NasaImageRepository` (de `domain`): habla con la API de la NASA, convierte sus respuestas al modelo de dominio y mantiene una caché en memoria. Depende solo de `domain`.

**Todo es `internal`** salvo `dataModule`. Ni `ui` ni `app` deben poder ver Retrofit, DTOs, mappers o la caché.

## Estructura

```
network/      OkHttpClientProvider (timeouts 15 s, logging solo en debug), RetrofitProvider (Gson)
remote/
  api/        NasaApi                 (interfaz de Retrofit)
  dto/        *Dto                    (reflejo del JSON de la NASA)
  ApiCall.kt  apiCall { }             (excepciones → DomainError)
  NasaRemoteDataSource                (PAGE_SIZE = 10)
mapper/       DTO → dominio: ItemMapper, ImageLinkMapper, SearchResponseMapper, TitleMapper, HtmlMapper
cache/        SearchCache
repository/   NasaImageRepositoryImpl
di/           dataModule (incluye networkModule)
```

## API

- **URL base:** `BuildConfig.NASA_BASE_URL` = `https://images-api.nasa.gov/`, definida en `data/build.gradle.kts`.
- **Búsqueda:** `GET search?q=&page=&page_size=10&media_type=image`.
- **Detalle:** `GET search?nasa_id=&media_type=image`.
- **Permiso de internet:** lo declara el `AndroidManifest.xml` de este módulo.

## Reglas de los DTOs (Gson)

- **Campos nulos:** todos los campos son **nullables**, porque Gson no respeta la null-safety de Kotlin.
- **`@SerializedName`** en cada campo, para usar camelCase y sobrevivir a R8.
- **R8:** `consumer-rules.pro` mantiene el paquete `remote.dto` para la build de release. Si mueves los DTOs de paquete, actualiza la regla.
- **Solo los campos que se usan.** Gson ignora el resto.

## Mappers

Convierten a dominio y limpian los datos. Son funciones de extensión `internal`, pequeñas y puras:

- **`ItemDto.toNasaImage()`** devuelve `null` si no hay `nasa_id` (se descarta con `mapNotNull`); el resto de campos usan valores por defecto.
- **Imágenes:** miniatura = enlace `rel = "preview"`; imagen del detalle = el `alternate` más ancho (nunca el original, que puede pesar varios MB).
- **`hasNextPage`:** existe un enlace `rel = "next"`.
- **`withoutHtml()`** quita etiquetas y entidades HTML de títulos y descripciones.
- **`completeTitle()`** restaura los títulos que la NASA corta con "..." usando la primera frase de la descripción. No se debe perder información.

## Errores

`apiCall { }` envuelve cada llamada de red y devuelve `Result`:

- `IOException` → `DomainError.Network()`.
- `HttpException` → `DomainError.Server(code)`.
- Cualquier otra → `DomainError.Unknown()`.
- **`CancellationException` se relanza siempre:** no la conviertas en error o romperás la cancelación de las búsquedas.

## Caché (`SearchCache`)

- **En memoria,** una única instancia (`single`) compartida por toda la app.
- **Clave:** búsqueda + página, normalizando con `trim().lowercase()`.
- **Tiempo de vida:** `DEFAULT_TIME_TO_LIVE` = 5 minutos, medido con `TimeSource.Monotonic`.
- **Tamaño:** máximo `DEFAULT_MAX_ENTRIES` = 100, descartando la menos usada (`LinkedHashMap` en orden de acceso).
- **Hilos:** métodos `@Synchronized`.
- **`findImage(id)`** busca una imagen en las páginas válidas, para abrir el detalle sin red.

`NasaImageRepositoryImpl` decide cuándo usarla:

- **`search`:** página válida en caché → se devuelve sin red. Si no, va a la red y **solo guarda los aciertos**: los errores nunca se cachean.
- **`getImage`:** primero `cache.findImage(id)`, luego la red.

## Hilos

- **El repositorio decide el hilo:** cada `Flow` termina en `.flowOn(ioDispatcher)`, así que la red y el mapeo nunca van por el hilo principal.
- **Dispatcher por constructor:** `ioDispatcher` se recibe con `Dispatchers.IO` por defecto. Por eso en Koin se registra con lambda, no con `singleOf`.

## DI (`di/`)

- **`networkModule`** (`internal`): OkHttp, Retrofit y `NasaApi` como `single`.
- **`dataModule`:** `includes(networkModule)`, `NasaRemoteDataSource` (`factoryOf`), `SearchCache` (`single`) y el repositorio (`single<NasaImageRepository> { ... }`).

## Tests (`src/test`)

- **API y repositorio:** con **MockWebServer 3** (`mockwebserver3`) y la fixture `src/test/resources/search_response.json`.
  - `createNasaApi()` (en `TestFixtures.kt`) crea una `NasaApi` contra el servidor falso.
  - `server.requestCount` permite comprobar si hubo o no petición de red.
- **Caché:** `TestTimeSource` (`kotlin.time`) para mover el reloj (`timeSource += 5.minutes`) sin esperar.
- **Dispatcher:** el repositorio se crea con `UnconfinedTestDispatcher()`.
- **Mappers:** se prueban como funciones puras, a partir de JSON leído con Gson.

Ejecutar:

```bash
./gradlew :data:testDebugUnitTest
```
