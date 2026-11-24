<div align="center">

# Wayfarer

### *Next-Generation Native Android Vector Maps & Turn-by-Turn Navigation*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3_Expressive-4285F4.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/Version-2.1.0-C699FF.svg?style=for-the-badge)](https://github.com/)
[![MapLibre](https://img.shields.io/badge/MapLibre_GL-v13.7.1-00A98F.svg?style=for-the-badge&logo=maplibre&logoColor=white)](https://maplibre.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<p align="center">
  <b>Wayfarer (v2.1.0)</b> is a sleek, privacy-focused, and high-performance native Android mapping application built with <b>Jetpack Compose</b>, <b>Material 3 Expressive Design System</b>, and <b>MapLibre Native GL</b>. Powered by <b>OpenStreetMap</b> and <b>OSRM</b>, it delivers real-time vector map rendering, multi-transport mode route planning, custom Canvas wavy trip progress scrubbers, adaptive GPS controls, and rich developer telemetry.
</p>

---

</div>

## Key Highlights

- **Material 3 Expressive Design System**: Bold high-contrast Obsidian Plum (`#120D18`) and Soft Lilac (`#FAF5FF`) surfaces, Electric Lavender (`#C699FF`) & Acid Lime (`#D8FF72`) accent pops, 32.dp rounded shapes, and tight-tracked display typography.
- **Custom Canvas Wavy Progress Scrubber**: Real-time animated sine-wave trip progress scrubber during live turn-by-turn navigation (inspired by Android 14/15 Expressive media scrubbers).
- **Stacked Overlapping Route Cards**: Expressive stacked route preview cards displaying oversized duration metrics (`44.sp`) and high-contrast pill badges.
- **Multi-Transport Mode Navigation**: Calculate routes tailored specifically for **Driving**, **Walking**, or **Cycling** with live ETAs, geometry line rendering, and map-matching.
- **Modular Stateless Overlay Architecture**: Modularized UI layer extracted into stateless overlays (`SearchOverlay`, `MapControlsOverlay`, `RoutePreviewSheet`, `ActiveNavigationOverlay`).
- **Persistent User Choices**: Automatic disk persistence of user theme modes, accent palettes, dynamic colors, and GPS update intervals via `SharedPreferences`.
- **Adaptive GPS Refresh Rate Control**: Adjust position updates dynamically from **1 second (High Accuracy)** to **8 seconds (Battery Saver)** via a dedicated M3 slider.
- **Privacy-First Architecture**: Zero external tracking or location telemetry uploads. All GPS fixes, route math, and map rendering happen 100% locally on your device.

---

## Features

### Material 3 Expressive Aesthetics
- High-contrast obsidian plum (`#120D18`) dark surfaces and soft lilac-tinted white (`#FAF5FF`) light surfaces.
- High-visibility acid lime (`#D8FF72`) and pastel coral (`#FF9B9B`) semantic highlight badges for active routes, ETAs, and CTA buttons.
- Tabular numbers (`tnum`) across all time, distance, and coordinate readouts for crisp visual alignment.

### Vector Maps & Styles
- Powered by **MapLibre GL Native Android SDK** (`v13.7.1`) and **OpenStreetMap**.
- One-tap style switching between **Light** (CARTO Positron), **Dark** (CARTO Dark Matter), and **Libre** vector map tiles.
- Smooth gesture panning, pinch-to-zoom, and automatic route bounding box camera adjustments (`LatLngBounds`).

### Multi-Transport Route Planning & Live Guidance
- Multi-step route planning (Select Origin $\rightarrow$ Select Destination $\rightarrow$ Generate Route).
- Supports **Driving (Car)**, **Walking (Foot)**, and **Cycling (Bike)** profiles via the OSRM REST API.
- Live navigation state tracking with map-matching, traveled vs. remaining distance, remaining duration, automatic off-route detection, and animated custom Canvas wavy progress scrubbing.

### Search & Geocoding
- Global geocoding and landmark search powered by OpenStreetMap's **Nominatim API**.
- Floating capsule search pill (`58.dp` height, `CircleShape`, `surfaceContainerHighest` fill) with tonal action avatar button.
- Local search history persistence with instant recent search recall and one-tap removal.

### Expressive Settings & Personalization
- Large expressive display header (`40.sp` bold title, circular `44.dp` back button).
- **Theme Mode**: System Default, Light Mode, Dark Mode.
- **Dynamic Color**: Material You wallpaper-matching on Android 12+ (API 31+).
- **GPS Refresh Rate Slider**: Customize hardware position updates between **1s** (maximum responsiveness) and **8s** (battery saving).
- **Accent Color Palettes**: Pick between 6 custom Material 3 color palettes (*Lavender Purple, Emerald Mint, Sunset Coral, Royal Violet, Amber Sand, Cyan Ocean*).

### Developer Telemetry Stream
- Dedicated **Logs for Geeks** window streaming live network HTTP GET calls, GPS satellite fixes, map camera idle events, and OSRM geometry parameters.

---

## Technology Stack

| Layer | Technology / Library |
| :--- | :--- |
| **Language** | [Kotlin 2.2.10](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3 Expressive) |
| **Map Rendering** | [MapLibre Native Android SDK](https://maplibre.org/) (`v13.7.1`) |
| **Geocoding API** | [OpenStreetMap Nominatim API](https://nominatim.org/) |
| **Routing Engine** | [Open Source Routing Machine (OSRM API)](https://project-osrm.org/) |
| **Networking** | [Retrofit 2](https://square.github.io/retrofit/) + [Gson](https://github.com/google/gson) |
| **Location Services** | [Google Play Services FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) (`v21.4.0`) |
| **Architecture** | Unidirectional Data Flow (UDF) + MVVM + Coroutines & StateFlow |

---

## Architecture

Wayfarer strictly follows modern Android **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)** guidelines with modular stateless UI components:

```
                            ┌───────────────────────────────┐
                            │    MainActivity / App Host    │
                            └───────────────┬───────────────┘
                                            │ AnimatedVisibility
                                            ▼
               ┌────────────────────────────────────────────────────────┐
               │                                                        │
               ▼                                                        ▼
  ┌───────────────────────────┐                            ┌───────────────────────────┐
  │         MapScreen         │                            │      SettingsScreen       │
  │   (State & Event Host)    │                            │   (Jetpack Compose UI)    │
  └─────────────┬─────────────┘                            └─────────────┬─────────────┘
                │                                                        │
                ├───────────────────────┬────────────────────────┐       │
                ▼                       ▼                        ▼       │
   ┌───────────────────────┐ ┌────────────────────┐ ┌──────────────────┐ │
   │     SearchOverlay     │ │ MapControlsOverlay │ │RoutePreviewSheet │ │
   └───────────────────────┘ └────────────────────┘ └──────────────────┘ │
                │                       │                        │       │
                └───────────────────────┼────────────────────────┴───────┘
                                        │ Observes StateFlow<MapState>
                                        ▼
                          ┌───────────────────────────┐
                          │       MapViewModel        │
                          │    (UDF State Holder)     │
                          └──────┬─────────────┬──────┘
                                 │             │
          ┌──────────────────────┘             └──────────────────────┐
          ▼                                                           ▼
┌──────────────────┐                                     ┌──────────────────────────┐
│  LocationClient  │                                     │UserPreferencesRepository │
│ (Fused Location) │                                     │  & GeocodingRepository   │
└──────────────────┘                                     └──────────────────────────┘
```

- **`MapState`**: Single source of truth containing camera position, user location, search query/results, active route, transport mode, settings, and developer logs.
- **`MapViewModel`**: Exposes immutable `StateFlow<MapState>`, manages location subscriptions, and executes asynchronous search, routing, and user preference persistence jobs.
- **`UserPreferencesRepository`**: Persists theme modes, color palettes, dynamic colors, and GPS update intervals to disk using `SharedPreferences`.
- **`DefaultLocationClient`**: Reactive `callbackFlow` wrapper around `FusedLocationProviderClient` supporting dynamic update interval changes.

---

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish (2024.1+) or newer.
- **JDK**: Java 11 or higher.
- **Android Device / Emulator**:
  - `compileSdk`: 37
  - `minSdk`: 26 (Android 8.0 Oreo+)
  - `targetSdk`: 37

### Build & Run Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/Wayfarer.git
   cd Wayfarer
   ```

2. **Open in Android Studio**:
   Launch Android Studio, select **Open**, and choose the `Wayfarer` directory. Allow Gradle sync to complete.

3. **Compile Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Execute Unit Test Suite**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## Testing

Wayfarer includes a comprehensive unit test suite (`MapViewModelTest.kt`) verifying core business logic:
- Initial state verification
- Route planning transitions and OSRM route calculations
- Origin & destination swapping, clearing, and error handling
- Multi-transport mode profile switching (`Drive`, `Walk`, `Cycle`)
- Adaptive GPS refresh rate updates and value clamping (1s–8s)
- User preferences persistence & state restoration
- Search query submission and geocoding response parsing
- Camera recentering and map style cycling
- Settings navigation state management

---

## Attributions & Acknowledgments

- **Map Data**: © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors.
- **Vector Tile Services**: © [CARTO](https://carto.com/) & [MapLibre](https://maplibre.org/).
- **Routing Engine**: [Open Source Routing Machine (OSRM)](https://project-osrm.org/).
- **Geocoding Engine**: [Nominatim API](https://nominatim.org/).
