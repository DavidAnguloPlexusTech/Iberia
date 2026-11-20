# Iberia · NASA Space Explorer

App Android nativa, hecha con **Kotlin** y **Jetpack Compose**, que busca y muestra imágenes de la **API pública de la NASA**.

---

## Índice

1. [Qué hace la app](#1-qué-hace-la-app)
2. [API pública elegida](#2-api-pública-elegida)
3. [Cómo ejecutarla](#3-cómo-ejecutarla)
4. [Arquitectura](#4-arquitectura)
5. [Decisiones principales](#5-decisiones-principales)
6. [Tests](#6-tests)
7. [Integración continua (CI)](#7-integración-continua-ci)
8. [Accesibilidad](#8-accesibilidad)
9. [Diseño](#9-diseño)
10. [Stack y versiones](#10-stack-y-versiones)
11. [Estructura del proyecto](#11-estructura-del-proyecto)
12. [Flujo de trabajo con Git](#12-flujo-de-trabajo-con-git)
13. [Licencias y créditos](#13-licencias-y-créditos)

---

## 1. Qué hace la app

| Pantalla | Qué muestra |
|---|---|
| **Splash** | El logo de la NASA latiendo dos veces; después navega a Home. |
| **Home** | Un buscador y una lista de resultados. Sin texto en el buscador muestra un **feed aleatorio** de imágenes. |
| **Detalle** | Foto grande, título, fecha, centro de la NASA, fotógrafo (si lo hay) y descripción. |

Comportamiento de Home:

- **Búsqueda mientras se escribe.** La búsqueda se lanza 400 ms después de la última tecla, sin necesidad de pulsar "buscar".
- **Cancelación.** Si escribes otra búsqueda antes de que termine la anterior, la anterior se cancela y sus resultados nunca llegan a la pantalla.
- **Scroll infinito.** Los resultados llegan de **10 en 10**: al acercarte al final de la lista se pide la siguiente página, una sola cada vez.
- **Feed aleatorio sin búsqueda.** Cada página de 10 viene de un tema distinto (galaxias, Marte, Apolo, Hubble…), y el feed no se acaba.
- **Caché.** Repetir una búsqueda en menos de 5 minutos no vuelve a llamar a la red, y el detalle de una imagen ya listada se abre sin red.
- **Estados.** Hay estado de carga, error con botón "Retry" y "sin resultados".

Además:

- **Temas.** Tema claro y oscuro (sigue al sistema).
- **Idioma.** Toda la app está **en inglés** (los textos y también los de las librerías).
- **Accesibilidad.** Funciona con TalkBack, Switch Access, teclado y fuente al 200% (ver [Accesibilidad](#8-accesibilidad)).

---

## 2. API pública elegida

**[NASA Image and Video Library](https://images.nasa.gov/docs/images.nasa.gov_api_docs.pdf)**, `https://images-api.nasa.gov/`

- **Sin API key.** El proyecto se ejecuta tal cual, sin configurar claves.
- **Buscador real, paginación e imágenes,** que es justo lo que necesita una app de búsqueda, lista y detalle.
- **Contenido visual y aeroespacial,** con miles de fotografías.

Endpoints que usa la app:

| Uso | Petición |
|---|---|
| Buscar (y el feed aleatorio) | `GET /search?q={texto}&page={n}&page_size=10&media_type=image` |
| Detalle de una imagen (si no está en caché) | `GET /search?nasa_id={id}&media_type=image` |

Las fotos se cargan desde `images-assets.nasa.gov`.

---

## 3. Cómo ejecutarla

### Requisitos

- **Android Studio** reciente, con soporte para AGP 9.5.
- **JDK 25.** Gradle lo pide en `gradle/gradle-daemon-jvm.properties`; si no lo tienes instalado, Gradle lo descarga solo.
- **Android SDK** con la plataforma 37. Android Studio la instala al sincronizar.
- **Dispositivo o emulador** con Android 7.0 (API 24) o superior.

### Desde Android Studio

1. Clona el repositorio y ábrelo en Android Studio.
2. Espera a que termine la sincronización de Gradle.
3. Elige la configuración **app** y pulsa ▶ Run.

### Desde la terminal

Compilar e instalar en un dispositivo o emulador conectado:

```bash
./gradlew installDebug
```

Solo compilar (debug y release, igual que la CI):

```bash
./gradlew assembleDebug assembleRelease
```

Tests unitarios de todos los módulos:

```bash
./gradlew :domain:test testDebugUnitTest
```

Tests de UI de accesibilidad (necesitan un emulador o dispositivo conectado):

```bash
./gradlew :ui:connectedDebugAndroidTest
```

Análisis estático con Android Lint:

```bash
./gradlew lintDebug
```

---

## 4. Arquitectura

**Clean Architecture** en cuatro módulos de Gradle, con **MVVM** en la capa de presentación.

```
┌────────────────────────────────────────────────────────────┐
│ app            Punto de entrada: Application, Activity y   │
│                arranque de Koin. Conoce a todos los módulos.│
└───────┬───────────────────┬────────────────────┬───────────┘
        │                   │                    │
┌───────▼──────┐    ┌───────▼───────┐    ┌───────▼───────┐
│ ui           │    │ domain        │    │ data          │
│ Presentación │───►│ Kotlin puro   │◄───│ Red, caché,   │
│ Compose,     │    │ Modelos,      │    │ DTOs, mappers,│
│ ViewModels,  │    │ casos de uso, │    │ repositorio   │
│ navegación   │    │ interfaces    │    │               │
└──────────────┘    └───────────────┘    └───────────────┘
```

**Regla de dependencias: `data → domain ← ui`.**

- **`domain`** no depende de nada de Android. Es Kotlin puro: modelos (`NasaImage`, `SearchPage`), errores (`DomainError`), la **interfaz** del repositorio y los casos de uso.
- **`data`** implementa esa interfaz: Retrofit, DTOs, mappers y caché. Todo es `internal` salvo su módulo de Koin, así que ni `ui` ni `app` pueden usar un DTO o Retrofit por error: el compilador lo impide.
- **`ui`** solo conoce `domain`: pide datos a los casos de uso y nunca sabe si vienen de la red o de la caché.
- **`app`** es el único módulo que los conoce a todos, porque es donde se conectan con Koin.

### Recorrido de una búsqueda

```
SearchBar ─► HomeViewModel ─► SearchImagesUseCase ─► NasaImageRepositoryImpl
                 (debounce,          (domain)              │
                  cancelación)                             ├─ SearchCache   (¿página válida? → sin red)
                                                           └─ NasaRemoteDataSource ─► Retrofit ─► NASA
```

### Capa de presentación (MVVM)

Cada pantalla tiene **dos archivos**:

- **`XViewModel.kt`** contiene:
  - El **estado** (`data class XState`), expuesto como `StateFlow`. Dentro del ViewModel es un `MutableStateFlow` privado y se actualiza con `update { it.copy(...) }`.
  - Los **eventos de navegación** (`sealed interface XEvent`), expuestos como `SharedFlow`.
  - Una **función pública por acción** del usuario, como `onQueryChange`, `loadNextPage`, `retry` o `onImageClick`.
- **`XScreen.kt`** contiene:
  - `XRoute`, que obtiene el ViewModel con Koin, recoge el estado y los eventos, y navega.
  - `XScreen`, **sin estado**: recibe el estado y callbacks, así que se puede previsualizar y testear sin ViewModel.

Los componentes reutilizables viven cada uno en su propio archivo, en `components/`.

---

## 5. Decisiones principales

### 5.1 Cancelación

Se resuelve en varias capas:

1. **Debounce.** `HomeViewModel` espera 400 ms sin cambios antes de buscar (`debounce`) e ignora textos repetidos (`distinctUntilChanged`). Escribir "apollo" letra a letra lanza **una** búsqueda, no seis.
2. **Cancelación explícita.** Cada carga se lanza en un `Job` (`loadJob`). Antes de empezar una nueva, el anterior se cancela con `loadJob?.cancel()`. Así, los resultados de una búsqueda vieja **nunca pueden sobrescribir** los de una más nueva, aunque la vieja responda después.
3. **La cancelación llega hasta la red.** Las llamadas de Retrofit son funciones `suspend` y se cancelan junto con la corrutina. `apiCall` relanza la `CancellationException` en lugar de convertirla en un error, para no romper ese mecanismo.
4. **Ámbito.** Todo corre en `viewModelScope`: si el usuario sale de la pantalla, cualquier petición en curso se cancela sola.

Lo comprueba `HomeViewModelTest`: lanza una búsqueda lenta, escribe otra antes de que responda y verifica que la primera nunca llega a terminar y que solo se muestran los resultados de la segunda.

### 5.2 Caché

Caché **en memoria**:

| Situación | Qué pasa |
|---|---|
| Hay una página válida (menos de 5 min) | Se devuelve **sin llamar a la red**. |
| La página ha caducado | Se pide a la red y **se actualiza** la caché. |
| No existe | Se pide a la red y se guarda. |
| La red falla | El error **no se guarda**; el siguiente intento vuelve a la red. |

Está en dos sitios de `data`:

- **`SearchCache`** solo sabe guardar y caducar:
  - **Clave:** búsqueda + página, ignorando mayúsculas y espacios (`" Apollo "` = `"apollo"`).
  - **Tiempo de vida:** **5 minutos**.
  - **Reloj:** `TimeSource.Monotonic` de `kotlin.time`, que no se ve afectado si cambia la hora del móvil. En los tests se sustituye por `TestTimeSource`, que permite "avanzar" el tiempo al instante.
  - **Tamaño:** máximo **100 entradas**. Al llenarse descarta la menos usada (LRU), con un `LinkedHashMap` en orden de acceso.
  - **Hilos:** acceso sincronizado (`@Synchronized`), porque se puede usar desde varias corrutinas a la vez.
- **`NasaImageRepositoryImpl`** decide cuándo usarla: primero mira la caché y, si no hay nada válido, va a la red y guarda el resultado.

**Dónde vive:** en la memoria del proceso, en una única instancia (`single` de Koin) compartida por toda la app.

- **Sobrevive** a cambiar de pantalla y a rotar el móvil.
- **Se pierde** al cerrar la app.
- **Lo que guarda** son los datos de la búsqueda; las fotos las cachea aparte Coil, en memoria y en disco.

**Extra:** el detalle de una imagen que ya está en una página cacheada se abre **sin petición de red** (`SearchCache.findImage`).

`domain` y `ui` no saben que existe la caché; cambiarla o quitarla solo afecta a `data`.

### 5.3 Paginación y feed aleatorio

- **Páginas de 10,** pedidas a la API **una a una** según haces scroll (`snapshotFlow` sobre el estado de la lista, sin provocar recomposiciones).
- **Búsqueda y feed** usan **el mismo camino** en el ViewModel; solo cambia de dónde sale cada página.
- **Feed aleatorio** (`GetRandomImagesUseCase`, en `domain`): al crearlo se barajan 15 temas con muchas imágenes y se elige una página de inicio al azar (1-20). Cada página usa el tema siguiente, así que no salen dos páginas seguidas del mismo tema. Al terminar la vuelta se pasa a la página siguiente de cada tema, sin repetir combinaciones. El `Random` se inyecta para poder testearlo.

### 5.4 Concurrencia

- **Coroutines y Flow** en toda la app. Los repositorios y los casos de uso devuelven `Flow<Result<T>>`.
- **Hilos:** el repositorio hace `flowOn(Dispatchers.IO)`, así que la red y el mapeo nunca van por el hilo principal. El dispatcher se recibe por constructor, con `Dispatchers.IO` por defecto, para poder sustituirlo en los tests.
- **UI:** el estado se observa con `collectAsStateWithLifecycle()` y los eventos se recogen solo mientras la pantalla es visible (`repeatOnLifecycle(STARTED)`).

### 5.5 Errores

- **Un tipo propio de dominio:** `sealed class DomainError` con `Network`, `Server(code)`, `NotFound` y `Unknown`. La UI muestra un mensaje distinto para cada uno.
- **`apiCall`** convierte cualquier excepción de red en un `DomainError`, así que la UI nunca ve excepciones de Retrofit ni de OkHttp.
- **Los errores se modelan como valores** (`Result`), no como excepciones que haya que recordar capturar.

### 5.6 Datos de la API

Gson, con DTOs `internal`, todos los campos nullables (Gson no respeta la null-safety de Kotlin) y `@SerializedName`. Los mappers a dominio limpian lo que viene de la API:

- **Descarte:** se descartan los resultados sin id, porque no se podrían abrir.
- **HTML:** se elimina el HTML de títulos y descripciones; algunas descripciones de la NASA traen `<a>`, `<b>`…
- **Títulos cortados:** se completan los títulos que la NASA corta con "...", tomando la frase completa del principio de la descripción. Así no se pierde información.
- **Imágenes:** se usa una miniatura para la lista y una imagen grande para el detalle (nunca la original, que puede pesar varios MB).

### 5.7 Inyección de dependencias

**Koin**, con un módulo por capa (`domainModule`, `dataModule`, `uiModule`) que `app` junta en `appModules`. Un test (`AppModulesTest`) verifica que todo el grafo de dependencias se puede resolver, para detectar fallos de configuración sin ejecutar la app.

---

## 6. Tests

**85 tests unitarios** y **27 tests de UI**. Los unitarios usan **Mockito** (con `mockito-kotlin`) para simular el repositorio en domain y presentación, **kotlinx-coroutines-test** para las corrutinas con tiempo virtual, y **MockWebServer** para probar la red real en data.

### Domain: casos de uso con repositorios simulados

| Clase | Qué comprueba |
|---|---|
| `SearchImagesUseCaseTest` (3) | Limpia los espacios de la búsqueda, pide la página indicada y devuelve lo que da el repositorio (`verify` sobre el mock). |
| `GetRandomImagesUseCaseTest` (5) | Temas y página de inicio válidos, tema distinto en cada página, cambio de vuelta y feed sin fin. |
| `GetImageUseCaseTest` (2) | Pide la imagen del id indicado y propaga los errores del repositorio. |

### Data: caché, red y mappers

| Clase | Qué comprueba |
|---|---|
| `SearchCacheTest` (10) | Guarda y lee, **sigue válida a los 4:59 y caduca a los 5:00**, volver a guardar reinicia el tiempo, cada página es independiente, ignora mayúsculas y espacios, descarta la menos usada al llenarse, y solo encuentra imágenes en páginas válidas. |
| `NasaImageRepositoryImplTest` (13) | Con MockWebServer **contando las peticiones reales**: caché válida = 1 sola petición, caducada = nueva petición y caché actualizada, otra página o búsqueda va a la red, los errores no se cachean, y el detalle cacheado no hace petición. También el tamaño de página, el mapeo y los errores HTTP. |
| `NasaApiTest` (3) | Parámetros que se envían a la API y lectura de la respuesta. |
| `ApiCallTest` (5) | Conversión de excepciones a `DomainError`, y que la cancelación no se pierde. |
| `SearchResponseMapperTest` (8), `TitleMapperTest` (5), `HtmlMapperTest` (4) | Mapeo de la respuesta, títulos cortados y limpieza de HTML. |

### Presentation

| Clase | Qué comprueba |
|---|---|
| `HomeViewModelTest` (16) | **Cancelación**: una búsqueda nueva cancela la que está en curso y solo se muestran sus resultados. También debounce (solo se busca la última consulta al escribir rápido), estado de carga, errores, reintentos, paginación, feed aleatorio y navegación. |
| `DetailViewModelTest` (6) | Pide la imagen de su id, carga, error, reintento y volver atrás. |
| `SplashViewModelTest` (1), `DateFormattingTest` (3) | Navegación desde la Splash y formato de fechas. |
| `AppModulesTest` (1) | Grafo de Koin completo. |

### UI de accesibilidad (`ui/src/androidTest`, en emulador)

| Clase | Qué comprueba |
|---|---|
| `TalkBackTest` (12) | Encabezados, cada resultado leído como un solo elemento, fechas en palabras, regiones vivas, orden de lectura y títulos de pantalla. |
| `FontScalingTest` (6) | Con la fuente al **200%** ningún texto se corta y los controles siguen siendo alcanzables y de al menos 48 dp. |
| `SwitchAccessTest` (9) | Todo lo interactivo tiene nombre, no hay paradas de más, cada control se activa con el teclado (Enter) y la lista carga páginas sin gestos. |

---

## 7. Integración continua (CI)

Dos workflows de **GitHub Actions** en `.github/workflows/`:

| Workflow | Qué hace |
|---|---|
| **Build** (`build.yml`) | Compila la app en debug y en release (la release también pasa R8). |
| **Tests** (`tests.yml`) | Ejecuta los tests unitarios de todos los módulos y sube los informes como artefacto, también cuando algo falla. |

- **Cuándo se ejecutan:** en cada push a `main`, `develop`, `feature/**`, `fix/**` y `release/**`, y en los pull requests hacia `main` y `develop`.
- **Entorno:** JDK 25 (Temurin), con caché de Gradle y validación del wrapper.
- **Ejecuciones duplicadas:** un push nuevo a la misma rama cancela la ejecución anterior.
- **Seguridad:** Dependabot (alertas, actualizaciones de seguridad y actualizaciones semanales de Gradle y Actions), code scanning con CodeQL y secret scanning. Las vulnerabilidades se reportan en privado según [`SECURITY.md`](SECURITY.md).

---

## 8. Accesibilidad

La app se ha preparado para tres formas de uso, y cada una tiene su batería de tests:

**TalkBack (lector de pantalla)**

- **Resultados:** cada resultado se lee como un único botón, por ejemplo "Earth Day 2017. Date December 8, 2017. Center GSFC", con la acción "Open details".
- **Fechas en palabras:** "December 8, 2017" en lugar de "2017-12-08".
- **Encabezados** en el título del detalle y del estado vacío.
- **Filas del detalle** leídas como frases ("Date: …").
- **Títulos de pantalla** para anunciar los cambios de pantalla.
- **Orden de lectura:** el buscador y el botón de volver se leen primero.
- **Regiones vivas:** la carga, los errores y "sin resultados" se anuncian solos.
- **Elementos decorativos** (iconos y foto del detalle) que no se leen.

**Escalado de fuente (hasta el 200%)**

- **Sin cortes:** ningún texto se corta; los títulos y valores largos bajan de línea.
- **Mensajes accesibles:** los mensajes de error y de estado vacío se pueden desplazar y suben por encima del teclado.
- **Áreas táctiles** de 48 dp como mínimo.
- **Previews** con `@PreviewFontScale` para revisar todas las escalas en Android Studio.

**Switch Access y teclado**

- **Una parada por elemento:** cada elemento interactivo es una sola parada, con nombre y rol.
- **Foco visible:** borde naranja de 2 dp en el elemento con foco.
- **Sin gestos:** todo funciona con "siguiente y seleccionar" (Tab y Enter), y el scroll por acciones de accesibilidad carga más páginas.

---

## 9. Diseño

Minimalista, inspirado en el **pop de la era espacial** de los años 60:

- **Paleta:** naranja atómico, turquesa y mostaza sobre crema (tema claro) o azul noche (tema oscuro).
- **Tipografía:** **Space Grotesk**, incluida en la app.
- **Formas:** esquinas de 4 dp como máximo, sin tarjetas ni sombras; la lista se separa con líneas finas.
- **Material Design 3** con un tema propio (`ui/theme/`).
- **Iconos:** vectores XML propios, sin depender de `material-icons`.

---

## 10. Stack y versiones

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.4.21 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Navigation Compose 2.10.2 (rutas type-safe) |
| Arquitectura | Clean Architecture + MVVM, Lifecycle 2.11.0 |
| Inyección de dependencias | Koin 4.2.2 |
| Asincronía | Coroutines y Flow 1.11.0 |
| Red | Retrofit 3.0.0, OkHttp 5.5.1, Gson |
| Imágenes | Coil 3.6.3 |
| Tests | JUnit 4, kotlinx-coroutines-test, Mockito 5.24.0 + mockito-kotlin 6.4.0, Turbine 1.2.1, MockWebServer, Compose UI Test, Espresso 3.7.0 |
| Build | Gradle 9.9.1, AGP 9.5.1, catálogo de versiones (`gradle/libs.versions.toml`) |
| SDK | `minSdk` 24 · `targetSdk` y `compileSdk` 37 |

---

## 11. Estructura del proyecto

```
app/            Application, MainActivity y arranque de Koin
domain/         Kotlin puro
  model/          NasaImage, SearchPage, RandomFeed
  error/          DomainError
  repository/     NasaImageRepository (interfaz)
  usecase/        SearchImages, GetImage, GetRandomImages
  di/             domainModule
data/
  network/        OkHttp y Retrofit
  remote/         NasaApi, DTOs, NasaRemoteDataSource, apiCall
  mapper/         DTO → dominio (HTML, títulos, enlaces de imagen)
  cache/          SearchCache
  repository/     NasaImageRepositoryImpl
  di/             dataModule y networkModule
ui/
  splash/ home/ detail/   ViewModel + Screen (y components/) por pantalla
  common/                 componentes compartidos y utilidades
  navigation/             Routes, IberiaNavHost
  theme/                  colores, tipografía y formas
  di/                     uiModule
.github/workflows/  CI: build.yml y tests.yml
licenses/           Licencia de la fuente Space Grotesk
```

---

## 12. Flujo de trabajo con Git

**Gitflow:**

- **`main`:** versiones publicadas.
- **`develop`:** integración.
- **`feature/*`:** cada funcionalidad nueva (módulos, Koin, Retrofit, UI, caché, CI, accesibilidad…).
- **`fix/*`:** las correcciones.

Cada rama se integra en `develop` con `merge --no-ff`, para que el historial muestre cada funcionalidad como un bloque.

---

## 13. Licencias y créditos

- **Datos e imágenes:** [NASA Image and Video Library](https://images.nasa.gov/). El contenido de la NASA es, en general, de dominio público; consulta sus [normas de uso](https://www.nasa.gov/nasa-brand-center/images-and-media/).
- **Logo de la NASA:** marca protegida de la NASA. Su uso aquí no indica ninguna relación ni respaldo de la NASA y habría que sustituirlo antes de publicar la app.
- **Fuente:** [Space Grotesk](https://github.com/floriankarsten/space-grotesk), con licencia SIL Open Font License 1.1 (ver `licenses/SpaceGrotesk-OFL.txt`).
