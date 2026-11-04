# Calculino

Calculadora Android con una interfaz amable, herramientas científicas, álgebra online, IA local y escaneo de fórmulas con cámara.

## Descarga

La versión publicada actualmente es un APK de depuración:

[Calculino 1.0.0 (debug)](https://github.com/Beniamino-B/Calculino/releases/tag/v1.0.0)

## Características

- Calculadora científica con historial y conversor de divisas.
- Álgebra local y resolución online mediante Math.js, con precisión configurable y caché de resultados.
- Asistente con IA local Qwen 2.5 1.5B para chat y explicación de cálculos.
- Escaneo de fórmulas con CameraX y reconocimiento de texto en el dispositivo con ML Kit.
- Onboarding configurable: tema, IA local, modelo y acceso a la cámara.
- Herramientas de ciencia e ingeniería: matrices, estadística, números complejos, energía de Gibbs, fórmula empírica, pH, subnet, operaciones bitwise, JSON/XML, división larga, geometría, física y tabla periódica.
- Temas claro, oscuro y automático.
- Interfaz en español, Material 3 y diseño adaptable.

## Tecnología

- Kotlin 2.0.21
- Jetpack Compose y Material 3
- Navigation Compose
- Room y DataStore
- Retrofit y OkHttp
- CameraX y ML Kit Text Recognition
- llama-android con Qwen 2.5 1.5B
- Math.js API

Arquitectura UDF con ViewModels, `StateFlow` y dependencias inyectadas manualmente desde `AppContainer`.

## Requisitos

- JDK 17
- Android SDK 35
- Android 8.0 (API 26) o superior
- Para IA local: dispositivo `arm64-v8a` con espacio y memoria suficientes

## Compilar

```bash
./gradlew testDebugUnitTest assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Configuración opcional

Las URLs de servicios y modelos se pueden sobrescribir al compilar:

```bash
./gradlew assembleDebug \
  -Pcalculino.mathJsBaseUrl=https://api.mathjs.org/ \
  -Pcalculino.qwenModelUrl=https://example.com/model.gguf
```

Las propiedades disponibles son:

- `calculino.mathJsBaseUrl`
- `calculino.qwenModelUrl`

## Estructura

```text
app/src/main/java/com/calculino/app/
├── data/          # IA, OCR, red, preferencias y persistencia
├── domain/        # Matemáticas, ciencia, ingeniería y modelos
└── ui/            # Compose, navegación, pantallas y ViewModels
```

## Privacidad

- El OCR y la IA local se ejecutan en el dispositivo.
- Las expresiones enviadas al modo online de álgebra se envían a Math.js.
- La descarga del modelo de IA se realiza desde la URL configurada.
- La cámara solo se utiliza al abrir el escáner de fórmulas.

## Estado del proyecto

La firma de release no está incluida en el repositorio. El APK publicado en GitHub Releases está firmado con la clave de depuración y no debe considerarse una build de producción.

## Contribuciones

Las incidencias y propuestas son bienvenidas mediante GitHub Issues.
