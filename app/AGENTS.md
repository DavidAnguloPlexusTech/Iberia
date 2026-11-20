# AGENTS.md · Proyecto Iberia (guía general)

Guía para asistentes de IA que trabajan en este repositorio. Léela antes de tocar código y después la guía del módulo que vayas a cambiar.

## Qué es la app

App Android nativa (Kotlin + Jetpack Compose) que busca imágenes en la API pública de la NASA (`https://images-api.nasa.gov/`, sin API key). Tiene tres pantallas:

- **Splash:** el logo de la NASA late dos veces y navega a Home.
- **Home:** buscador más lista paginada de 10 en 10. Sin texto, muestra un feed aleatorio.
- **Detalle:** foto, título, fecha, centro, fotógrafo y descripción.

Prioridades del proyecto: **simple, legible y escalable**, con clean code, funciones pequeñas y clases cortas. Ante la duda, elige la solución más simple.

## Módulos y guías

| Módulo | Qué contiene | Guía |
|---|---|---|
| `app` | Punto de entrada: `IberiaApplication` (arranca Koin), `MainActivity` y `di/AppModules.kt` | Este archivo |
| `domain` | Kotlin puro: modelos, errores, interfaz del repositorio y casos de uso | [`../domain/AGENTS.md`](../domain/AGENTS.md) |
| `data` | Red (Retrofit), DTOs, mappers, caché en memoria e implementación del repositorio | [`../data/AGENTS.md`](../data/AGENTS.md) |
| `ui` | Presentación: Compose, ViewModels (MVVM), navegación, tema y accesibilidad | [`../ui/AGENTS.md`](../ui/AGENTS.md) |

**Regla de dependencias: `data → domain ← ui`.**

- **`domain`** no depende de ningún otro módulo ni de Android.
- **`data` y `ui`** dependen de `domain`, nunca entre sí.
- **`app`** es el único que conoce a los tres; solo los conecta con Koin.

Si una implementación te obliga a romper esta regla, el diseño está mal: replantéalo.

## Convenciones globales (obligatorias)

- **Sin comentarios en el código** (ni KDoc, ni `//`, ni comentarios en XML, Gradle o YAML). Usa nombres claros y funciones pequeñas. Las explicaciones van en el README o en estos `AGENTS.md`.
- **Sin APIs deprecated ni sobrecargas antiguas.** Usa `kotlin.time.Duration` (`400.milliseconds`) en lugar de `Long` de milisegundos o `TimeUnit`, y `mockwebserver3` en lugar de `okhttp3.mockwebserver`. Antes de terminar, `./gradlew lintDebug` debe decir *No issues found*.
- **Código en `src/main/kotlin`,** nunca en `java/`.
- **Mínima visibilidad:** `internal` para todo lo que no tenga que salir de su módulo; `private` dentro del archivo.
- **App solo en inglés:** los textos van en `ui/src/main/res/values/strings.xml`. No hay otros idiomas, y `app` tiene `localeFilters += "en"`.
- **Versiones:** todas las dependencias van en `gradle/libs.versions.toml` (catálogo de versiones), siempre en su última versión estable.
- **Sin referencias externas en la documentación:** el README y estas guías describen la app como un producto.

## Inyección de dependencias (Koin)

Cada capa tiene su módulo en su paquete `di/`: `domainModule`, `dataModule` (que incluye `networkModule`) y `uiModule`. `app/di/AppModules.kt` los junta en `appModules`, e `IberiaApplication` llama a `startKoin`.

- **Siempre lambda con argumentos con nombre:** `factory { X(repository = get()) }`, `single { ... }` y `viewModel { ... }`, de modo que se vea qué recibe cada clase. **No uses** el constructor DSL (`factoryOf`, `singleOf`, `viewModelOf`).
- **Valores por defecto** (`Dispatchers.IO`, `Random.Default`): no se pasan en la lambda, así se usa el del constructor.
- **Parámetros en tiempo de ejecución** con `parametersOf`, recogidos con `params ->`: `viewModel { params -> DetailViewModel(nasaId = params.get(), getImage = get()) }`.
- **Test del grafo:** `app/src/test/.../di/AppModulesTest.kt` verifica todo el grafo con `verify()`. Si añades una definición con un tipo que Koin no puede resolver (por ejemplo, un `String` que llega por parámetros), añádelo a `extraTypes`.

## Cómo añadir una funcionalidad nueva

1. **`domain`:** modelo, método en `NasaImageRepository` (si hace falta) y caso de uso. Regístralo en `domainModule`.
2. **`data`:** endpoint en `NasaApi`, DTO, mapper y la implementación en `NasaImageRepositoryImpl`. Regístralo en `dataModule` si hay clases nuevas.
3. **`ui`:** `XViewModel.kt` (estado, eventos y ViewModel) y `XScreen.kt` (Route, pantalla sin estado y Preview). Componentes en `components/`, ruta en `navigation/Routes.kt`, destino en `IberiaNavHost.kt` y registro en `uiModule`.
4. **Tests** en cada capa tocada (ver las guías de cada módulo).
5. **Verificación** con los comandos de abajo.

## Comandos

Compilar (debug y release, igual que la CI):

```bash
./gradlew assembleDebug assembleRelease
```

Tests unitarios de todos los módulos:

```bash
./gradlew :domain:test testDebugUnitTest
```

Lint (debe quedar sin incidencias):

```bash
./gradlew lintDebug
```

Tests de UI de accesibilidad (necesitan un emulador o dispositivo conectado):

```bash
./gradlew :ui:connectedDebugAndroidTest
```

- **Entorno:** Gradle exige **JDK 25** (`gradle/gradle-daemon-jvm.properties`). En esta máquina se usa el JDK de Android Studio (`JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`).
- **Emulador:** `Medium_Phone_API_37.0`.

## CI (GitHub Actions)

En `.github/workflows/`:

- **`build.yml`** compila debug y release.
- **`tests.yml`** ejecuta los tests unitarios y sube los informes.

Se lanzan en cada push a `main`, `develop`, `feature/**`, `fix/**` y `release/**`, y en los pull requests hacia `main` y `develop`. No dejes una rama con la CI en rojo.

## Git (gitflow)

- **Ramas:** `main` (versiones publicadas) y `develop` (integración). Las funcionalidades van en `feature/*` y las correcciones en `fix/*`, nunca `feature/` para un arreglo.
- **El usuario decide** el nombre de cada rama y el mensaje de cada commit. No hagas commit, push ni merge sin que lo pida.
- **Merge:** a `develop` con `merge --no-ff`, sin borrar la rama de la funcionalidad.
