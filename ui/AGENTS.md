# AGENTS.md · Módulo `ui` (presentación)

Guía general del proyecto: [`../app/AGENTS.md`](../app/AGENTS.md).

## Qué es

Toda la interfaz en **Jetpack Compose + Material 3**, con **MVVM**. Depende solo de `domain`: usa sus casos de uso y modelos y nunca conoce `data` (ni Retrofit, ni DTOs, ni la caché).

## Estructura

```
IberiaApp.kt              raíz: IberiaTheme + Surface + IberiaNavHost
splash/  home/  detail/   una carpeta por pantalla
  XViewModel.kt           XState + XEvent + XViewModel
  XScreen.kt              XRoute + XScreen + Previews
  components/             un composable por archivo
common/
  components/             componentes compartidos (NasaAsyncImage, LoadingAnimation, ErrorMessage,
                          EmptyMessage, ScrollableCenter, FocusableIconButton)
  *.kt                    utilidades (fechas, mensajes de error, focusBorder, PreviewData)
navigation/               Routes.kt (rutas @Serializable), IberiaNavHost.kt, NavControllerExtensions.kt
theme/                    Color, Type (Space Grotesk), Shape, IberiaTheme
di/                       uiModule
```

## Cómo se hace una pantalla (MVVM)

**Dos archivos por pantalla**, sin clases base ni utilidades compartidas para el estado o los eventos:

**`XViewModel.kt`**, en este orden:

1. `internal data class XState(...)`: estado inmutable. Se llama **`State`**, nunca `UiState`.
2. `internal sealed interface XEvent`: eventos de un solo uso (navegación).
3. `internal class XViewModel(...) : ViewModel()`, que contiene:
   - **Estado:** `private val _state = MutableStateFlow(XState())` y `val state: StateFlow<XState> = _state.asStateFlow()`.
   - **Eventos:** `private val _events = Channel<XEvent>(Channel.BUFFERED)` y `val events: Flow<XEvent> = _events.receiveAsFlow()`. No uses `MutableSharedFlow`: sin colector activo descarta el evento.
   - **Cambios de estado** solo con `_state.update { it.copy(campoQueCambia = ...) }`, sin funciones ni archivos aparte para actualizar el estado.
   - **Una función pública por acción** del usuario (`onQueryChange`, `retry`, `onBackClick`…). Nada de `Intent` ni `onIntent`: es MVVM, no MVI.
   - **Corrutinas** en `viewModelScope`. Los eventos se envían con `viewModelScope.launch { _events.send(...) }`.

**`XScreen.kt`:**

1. **`XRoute`** obtiene el ViewModel con `koinViewModel()`, recoge el estado con `collectAsStateWithLifecycle()` y los eventos dentro de `LaunchedEffect` + `repeatOnLifecycle(Lifecycle.State.STARTED)` + `withContext(Dispatchers.Main.immediate)`, y navega.
2. **`XScreen`** es sin estado: recibe `state` y callbacks, sin ViewModel. Así se puede previsualizar y testear.
3. **Previews** con `IberiaTheme`, y `@PreviewFontScale` en las que tienen texto.

Los parámetros de navegación (como `nasaId` en Detail) llegan al ViewModel por `parametersOf` desde la ruta: `viewModel { params -> DetailViewModel(nasaId = params.get(), getImage = get()) }`.

## Home: búsqueda, cancelación y paginación

`HomeViewModel` es la pieza con más lógica:

- **Búsqueda:** `queries` (`MutableStateFlow`) → `debounce(SEARCH_DEBOUNCE)` (400 ms) → `trim` → `onQuerySettled`, que solo busca si el texto cambió o si la búsqueda anterior se canceló.
- **Cancelación:** cada carga se lanza en `loadJob`. `onQueryChange` la cancela en cuanto el texto cambia, sin esperar al debounce, y vacía la lista mostrando el estado de carga (salvo que solo cambien espacios), y `load()` la cancela antes de cada carga nueva. Así una búsqueda vieja nunca llega a mostrarse. Mantén este patrón en cualquier carga nueva.
- **Dos fuentes, un solo camino:** con texto se usa `SearchImagesUseCase` y sin texto el feed aleatorio (`GetRandomImagesUseCase`), las dos con la misma paginación.
- **Paginación:** la lista (`ImageList`) observa el scroll con `snapshotFlow` y llama a `loadNextPage()` cuando faltan 5 elementos. El ViewModel ignora la llamada si `canLoadNextPage` es falso. Al añadir páginas se eliminan ids repetidos, porque `LazyColumn` usa el id como clave.

## Diseño

- **Estilo:** minimalista, pop de la era espacial.
- **Paleta** (en `theme/Color.kt`): naranja atómico (primario), turquesa (secundario) y mostaza (terciario), sobre crema (claro) o azul noche (oscuro).
- **Tipografía:** Space Grotesk (fuente variable en `res/font`), aplicada en `theme/Type.kt`.
- **Formas:** esquinas de 4 dp como máximo (`theme/Shape.kt`). Sin tarjetas, sombras ni bordes decorativos; las listas se separan con `HorizontalDivider` fino.
- **Iconos:** vectores XML propios en `res/drawable` (no uses `material-icons`).
- **Textos:** siempre en `res/values/strings.xml`, en inglés.
- **Imágenes remotas:** siempre con `NasaAsyncImage` (Coil).

## Accesibilidad (obligatoria en cualquier composable nuevo)

**TalkBack:**

- **Elementos compuestos** (como una fila de resultado) se leen como uno: `clickable(onClickLabel = ..., role = Role.Button)` + `clearAndSetSemantics { contentDescription = ... }`.
- **Encabezados:** `semantics { heading() }` en los títulos.
- **Regiones vivas:** `liveRegion = LiveRegionMode.Polite` en carga, errores y estados vacíos.
- **Títulos de pantalla:** `paneTitle` en el `Scaffold`, con `isTraversalGroup = true`. La barra superior va con `traversalIndex = -1f`.
- **Iconos e imágenes decorativos:** `contentDescription = null`.
- **Fechas:** se leen en palabras con `toAccessibleDate()`.

**Escalado de fuente (hasta el 200%):**

- **Sin cortes:** nunca `maxLines` ni `TextOverflow.Ellipsis` que corten información.
- **Ancho:** los textos de una fila usan `weight(1f)` para bajar de línea.
- **Contenido alto:** si puede no caber, usa `ScrollableCenter` o un contenedor con scroll.
- **Teclado:** el contenido de Home usa `imePadding()` para quedar por encima del teclado.
- **Áreas táctiles** de al menos 48 dp.

**Switch Access y teclado:**

- **Nombres:** todo lo interactivo debe tener nombre (texto o `contentDescription`).
- **Foco visible:** `focusBorder(...)` con el `interactionSource` del control, o `FocusableIconButton` para iconos.
- **Paradas:** nada que no sea interactivo debe ser clicable.

## Tests

**Unitarios (`src/test`): ViewModels**

- **JUnit 5 (Jupiter).** La extensión **`MainDispatcherExtension`** (`@JvmField @RegisterExtension`) sustituye `Dispatchers.Main` por un `StandardTestDispatcher`.
- **Tiempo virtual:** `advanceTimeBy(SEARCH_DEBOUNCE)` y `advanceUntilIdle()`.
- **Eventos:** con Turbine (`events.test { ... awaitItem() }`).
- **Mockito:**
  - El repositorio se simula con `mock()`.
  - En `@BeforeEach`, `whenever(...).thenAnswer { flow { delay(...); emit(...) } }` simula resultados por página y retrasos, con variables del test (`searchResults`, `responseDelay`, `imageResult`).
  - Las búsquedas realizadas se leen con `mockingDetails(repository).invocations` (helper `searches()` en `HomeViewModelTest`), y las llamadas concretas se comprueban con `verify`.
- **Datos de ejemplo:** `image()` y `searchPage()` están en `TestData.kt`.
- **Ojo:** los ViewModels se crean con `by lazy`; tócalos (`viewModel`) antes de avanzar el tiempo si el test depende de su `init`.

```bash
./gradlew :ui:testDebugUnitTest
```

**UI de accesibilidad (`src/androidTest/.../accessibility/`), en emulador o dispositivo, con JUnit 4** (lo exigen `createComposeRule` y `AndroidJUnitRunner`)**:**

- **`TalkBackTest`:** semántica para TalkBack.
- **`FontScalingTest`:** con `DeviceConfigurationOverride.FontScale(2f)`.
- **`SwitchAccessTest`:** pide `InputMode.Keyboard` antes de enfocar y pulsar Enter, porque los elementos clicables solo reciben foco en modo teclado.
- **Matchers propios** en `SemanticsMatchers.kt`.
- **Espresso:** se fija `espresso-core` 3.7.0 en `ui/build.gradle.kts`, porque la versión que trae Compose UI Test no funciona en Android reciente.

```bash
./gradlew :ui:connectedDebugAndroidTest
```

Si añades un composable interactivo o con texto, añade su caso en estos tests.
