<div align="center">

# 🗺️ Wayfarer

### *Next-Generation Native Android Vector Maps & Turn-by-Turn Navigation*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![MapLibre](https://img.shields.io/badge/MapLibre_GL-v13.7.1-00A98F.svg?style=for-the-badge&logo=maplibre&logoColor=white)](https://maplibre.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-2ea44f.svg?style=for-the-badge&logo=github)](https://github.com/)

<p align="center">
  <b>Wayfarer</b> is a sleek, privacy-focused, and high-performance native Android mapping application built with <b>Jetpack Compose</b>, <b>Material 3 (Material You)</b>, and <b>MapLibre Native GL</b>. Powered by <b>OpenStreetMap</b> and <b>OSRM</b>, it delivers real-time vector map rendering, multi-transport mode route planning, adaptive GPS update controls, and rich developer telemetry.
</p>

---

</div>

## 🌟 Key Highlights

- 🚗🚶🚴 **Multi-Transport Mode Navigation**: Calculate routes tailored specifically for **Driving**, **Walking**, or **Cycling** with live ETAs, geometry line rendering, and map-matching.
- ⚙️ **Dedicated Settings & Appearance Page**: Seamless animated full-screen navigation to customize app themes, dynamic colors, accent palettes, and location settings.
- ⚡ **Adaptive GPS Refresh Rate Control**: Adjust position updates dynamically from **1 second (High Accuracy)** to **8 seconds (Battery Saver)** via a dedicated M3 slider.
- 🎨 **Material You & Custom Accent Palettes**: Full dark/light/system theme support with Material You wallpaper dynamic colors and 6 curated accent color palettes (*Lavender Purple, Emerald Mint, Sunset Coral, Royal Violet, Amber Sand, Cyan Ocean*).
- 🤓 **Developer Telemetry ("Logs for Geeks")**: Built-in real-time telemetry log window monitoring HTTP REST requests, GPS satellite metrics, and OSRM route parameters.
- 🔒 **Privacy-First Architecture**: Zero external tracking or location telemetry uploads. All GPS fixes, route math, and map rendering happen 100% locally on your device.

---

## ✨ Features

### 🗺️ Vector Maps & Styles
- Powered by **MapLibre GL Native Android SDK** (`v13.7.1`) and **OpenStreetMap**.
- One-tap style switching between **Light** (CARTO Positron), **Dark** (CARTO Dark Matter), and **Libre** vector map tiles.
- Smooth gesture panning, pinch-to-zoom, and automatic route bounding box camera adjustments (`LatLngBounds`).

### 🚗🚶🚴 Multi-Transport Route Planning
- Multi-step route planning (Select Origin $\rightarrow$ Select Destination $\rightarrow$ Generate Route).
- Supports **Driving (Car)**, **Walking (Foot)**, and **Cycling (Bike)** profiles via the OSRM REST API.
- Instant mode switching directly from the active route sheet to compare travel times across modes.
- Real-time navigation state tracking with map-matching, traveled vs. remaining distance, remaining duration, and automatic off-route detection.

### 🔍 Search & Geocoding
- Global geocoding and landmark search powered by OpenStreetMap's **Nominatim API**.
- Local search history persistence with instant recent search recall and one-tap removal.
- Selected place pin annotations with custom bottom sheet details.

### ⚙️ Comprehensive Settings & Personalization
- Dedicated full-screen **Settings** page accessible with smooth screen transition animations.
- **Theme Mode**: System Default, Light Mode, Dark Mode.
- **Dynamic Color**: Material You wallpaper-matching on Android 12+ (API 31+).
- **GPS Refresh Rate Slider**: Customize hardware position updates between **1s** (maximum responsiveness) and **8s** (battery saving).
- **Accent Color Palettes**: Pick between 6 custom Material 3 color palettes.

### 🤓 Developer Telemetry Stream
- Dedicated **Logs for Geeks** window streaming live network HTTP GET calls, GPS satellite fixes, map camera idle events, and OSRM geometry parameters.

---

## 🛠️ Technology Stack

| Layer | Technology / Library |
| :--- | :--- |
| **Language** | [Kotlin 2.2.10](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3) |
| **Map Rendering** | [MapLibre Native Android SDK](https://maplibre.org/) (`v13.7.1`) |
| **Geocoding API** | [OpenStreetMap Nominatim API](https://nominatim.org/) |
| **Routing Engine** | [Open Source Routing Machine (OSRM API)](https://project-osrm.org/) |
| **Networking** | [Retrofit 2](https://square.github.io/retrofit/) + [Gson](https://github.com/google/gson) |
| **Location Services** | [Google Play Services FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) (`v21.4.0`) |
| **Architecture** | Unidirectional Data Flow (UDF) + MVVM + Coroutines & StateFlow |

---

## 📐 Architecture

Wayfarer strictly follows modern Android **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)** guidelines:

```
                            ┌───────────────────────────────┐
                            │    MainActivity / App Host    │
                            └───────────────┬───────────────┘
                                            │ AnimatedContent
                                            ▼
               ┌────────────────────────────────────────────────────────┐
               │                                                        │
               ▼                                                        ▼
  ┌───────────────────────────┐                            ┌───────────────────────────┐
  │         MapScreen         │                            │      SettingsScreen       │
  │   (Jetpack Compose UI)    │                            │   (Jetpack Compose UI)    │
  └─────────────┬─────────────┘                            └─────────────┬─────────────┘
                │                                                        │
                └───────────────────────────┬────────────────────────────┘
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
    │  LocationClient  │                                     │   GeocodingRepository    │
    │ (Fused Location) │                                     │    & RouteRepository     │
    └──────────────────┘                                     │ (Nominatim & Retrofit)   │
                                                             └──────────────────────────┘
```

- **`MapState`**: Single source of truth containing camera position, user location, search query/results, active route, transport mode, settings, and developer logs.
- **`MapViewModel`**: Exposes immutable `StateFlow<MapState>`, manages location subscriptions, and executes asynchronous search and routing jobs.
- **`DefaultLocationClient`**: Reactive `callbackFlow` wrapper around `FusedLocationProviderClient` supporting dynamic update interval changes.
- **`OsrmRouteRepository`**: Performs asynchronous REST requests to OSRM endpoints (`/driving`, `/walking`, `/bike`).

---

## 🚀 Getting Started

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

## 🧪 Testing

Wayfarer includes a comprehensive unit test suite (`MapViewModelTest.kt`) verifying core business logic:
- Initial state verification
- Route planning transitions and OSRM route calculations
- Origin & destination swapping, clearing, and error handling
- Multi-transport mode profile switching (`Drive`, `Walk`, `Cycle`)
- Adaptive GPS refresh rate updates and value clamping (1s–8s)
- Search query submission and geocoding response parsing
- Camera recentering and map style cycling
- Settings navigation state management

---

## 📄 Attributions & Acknowledgments

- **Map Data**: © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors.
- **Vector Tile Services**: © [CARTO](https://carto.com/) & [MapLibre](https://maplibre.org/).
- **Routing Engine**: [Open Source Routing Machine (OSRM)](https://project-osrm.org/).
- **Geocoding Engine**: [Nominatim API](https://nominatim.org/).
