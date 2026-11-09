# Wayfarer 🗺️

**Wayfarer** is a minimalist, modern native Android maps and navigation application built with **Jetpack Compose**, **Material 3**, and **MapLibre Native Android**. It offers interactive vector map rendering, real-time GPS location tracking, place search via Nominatim, and route calculation powered by the Open Source Routing Machine (OSRM).

---

## ✨ Features

- **Interactive Vector Maps**:
  - Crisp vector tile rendering powered by **MapLibre Native Android SDK**.
  - Geographic data derived from **OpenStreetMap**.
  - One-tap style switcher for **Light** (CARTO Positron), **Dark** (CARTO Dark Matter), and **Libre** map styles.
  - Smooth camera panning, zoom controls, and attribution displays.
  - Default view centered on **Kolkata, India** (`22.5726° N, 88.3639° E`).

- **Real-time GPS Location**:
  - Precise location tracking powered by Google's **Fused Location Provider Client**.
  - Handles runtime fine and coarse location permissions gracefully.
  - Custom blue pulsing location dot with compass heading and accuracy indicator.
  - One-tap camera recentering on device location.

- **Place Search & Geocoding**:
  - Global place, street, and landmark search powered by OpenStreetMap's **Nominatim API**.
  - Search-on-submit architecture respecting API rate limits.
  - Rich search results with place name, detailed address, category badges, and coordinates.
  - Destination pin marker placement on the map upon selecting a result.

- **OSRM Route Calculation**:
  - Driving route calculation using **OSRM (Open Source Routing Machine) API** via **Retrofit 2**.
  - Multi-step route planning (Select Origin $\rightarrow$ Select Destination $\rightarrow$ Generate Route).
  - Support for using **Current GPS Location** as the route origin.
  - Real-time rendering of route geometry (GeoJSON LineString) as a vector polyline on the map.
  - Automatic camera viewport fitting (`LatLngBounds`) for the entire route.
  - Route summary card displaying formatted distance (km / m) and estimated duration (hours / mins).

- **Modern UI & Design**:
  - Follows **Material 3 (Material You)** design guidelines.
  - Custom **Wayfarer Slate** dark and light color palettes.
  - Unobtrusive floating controls prioritizing the map view.

---

## 🛠️ Technology Stack

| Component | Library / Framework |
| :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) |
| **Design System** | [Material 3](https://m3.material.io/) |
| **Map Engine** | [MapLibre Native Android SDK](https://maplibre.org/) (`v13.7.1`) |
| **Geocoding** | [OpenStreetMap Nominatim API](https://nominatim.org/) |
| **Routing Engine** | [OSRM Public Route API](https://project-osrm.org/) |
| **HTTP Client** | [Retrofit 2](https://square.github.io/retrofit/) + [Gson](https://github.com/google/gson) |
| **Location Services** | [Google Play Services Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) (`v21.4.0`) |
| **Architecture** | MVVM (Model-View-ViewModel) + Coroutines & StateFlow |

---

## 📐 Architecture

Wayfarer follows clean **MVVM (Model-View-ViewModel)** architecture:

```
                          ┌───────────────────────────┐
                          │         MapScreen         │
                          │   (Jetpack Compose UI)    │
                          └─────────────┬─────────────┘
                                        │
                                        │ Observes StateFlow<MapState>
                                        ▼
                          ┌───────────────────────────┐
                          │       MapViewModel        │
                          │   (State & UI Logic)      │
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

- **`MapState`**: Immutable data class holding camera position, map style, user location, search query/results, active route, and UI messages.
- **`MapViewModel`**: Manages application state, streams location updates, handles user interactions, and triggers asynchronous search & route calculation requests.
- **Data Repositories**:
  - **`DefaultLocationClient`**: Streams GPS updates using `FusedLocationProviderClient`.
  - **`NominatimGeocodingRepository`**: Fetches place search results asynchronously using Nominatim API.
  - **`OsrmRouteRepository`**: Performs HTTP route calculation requests via Retrofit 2 using OSRM's driving endpoint.

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Jellyfish (2024.1+) or newer.
- **JDK**: Java 11 or higher.
- **Android SDK**:
  - `compileSdk`: 37 (or 35+)
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk`: 37

### Building the Project

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/Wayfarer.git
   cd Wayfarer
   ```

2. **Open in Android Studio**:
   Open the project directory in Android Studio and wait for Gradle sync to complete.

3. **Build the Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew app:testDebugUnitTest
   ```

---

## 📱 License & Attribution

- **Map Data**: © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors.
- **Vector Styles**: © [CARTO](https://carto.com/) & [MapLibre](https://maplibre.org/).
- **Routing Engine**: [Open Source Routing Machine (OSRM)](https://project-osrm.org/).
