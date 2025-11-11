# Kotlin Music Player

A modern, native Android music player app built using Kotlin, Jetpack Compose, and an MVVM architecture. This app demonstrates core Android fundamentals, clean code structure, and a robust user experience by fetching and streaming music from a live API.

This project was built as an assessment to demonstrate proficiency in modern Android development.

---

## 🚀 Get the App

You can download and install the latest APK directly from the **Releases** page.

**[➡️ Download the latest APK here](https://github.com/Pratham-Bajpai1/Music-Player-App/releases/tag/v1.0)**

---

## 🚀 Features

* **Dynamic Track Loading:** Fetches a list of popular tracks from the Jamendo API.
* **Full Playback Controls:**
    * Play, Pause, and Seek functionality.
    * Real-time playback progress bar.
* **Modern Streaming:** Uses **ExoPlayer (Media3)** for fast, efficient streaming and buffering.
* **Robust UX:**
    * **Loading Indicators:** Shows a "Buffering..." indicator while the selected track is loading.
    * **Error Handling:** Intelligently detects "No Internet" on startup and provides a "Retry" button.
    * **Playback Errors:** Gracefully handles and displays playback or network-related errors.
* **Modern UI (Material 3):**
    * Built entirely with Jetpack Compose.
    * Collapsing Large Top App Bar for a spacious feel.
    * Responsive, `Card`-based list that highlights the currently playing track.
* **Data Sorting:**
    * Sort by track name (A-Z).
    * Sort by track duration (shortest to longest).

---

## 🛠 Tech Stack & Architecture

This app is built following a clean, scalable MVVM (Model-View-ViewModel) architecture.

* **Architecture:** **MVVM**
    * **View (UI):** `MainActivity.kt` (Jetpack Compose)
    * **ViewModel:** `MusicViewModel.kt` (Manages all UI state and business logic using `StateFlow`).
    * **Model (Data):** `data` package (Contains API services and data models).
* **Core Technology:**
    * **UI:** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose).
    * **Networking:** [Ktor Client](https://ktor.io/docs/client-overview.html) (A modern, coroutine-based networking library).
    * **Media Playback:** [ExoPlayer (Media3)](https://developer.android.com/guide/topics/media/exoplayer) (Google's standard for media playback).
    * **Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) (For all async operations and state management).
    * **JSON Parsing:** [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).
    * **Image Loading:** [Coil](https://coil-kt.github.io/coil/) (A lightweight, Kotlin-first image loader).

---

## 🏃 How to Run the App

You will need a recent version of **Android Studio (Iguana or newer)** to build and run this project.

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/Pratham-Bajpai1/Music-Player-App.git](https://github.com/Pratham-Bajpai1/Music-Player-App.git)
    ```

2.  **Get a Free API Key:**
    This app uses the **Jamendo API**. You must get a free `client_id` to make API calls.
    * Go to [https://developer.jamendo.com/](https://developer.jamendo.com/) and create a free developer account.
    * Create a new app in their developer dashboard.
    * You will be given a `client_id`.

3.  **Add Your API Key:**
    * Open the project in Android Studio.
    * Navigate to the `app/src/main/java/com/example/musicplayerapp/data/network/MusicApiService.kt` file.
    * Find this line:
        ```kotlin
        private val clientId = "YOUR_CLIENT_ID"
        ```
    * Replace `"YOUR_CLIENT_ID"` with the key you got from the Jamendo dashboard.

4.  **Build & Run:**
    * Let Android Studio sync the Gradle files.
    * Click the "Run" button to build and install the app on an emulator or a physical device.

---

## 📝 Decisions & Assumptions

### 1. API Choice: Jamendo (vs. Audiomack)

> The original assessment suggested the Audiomack API. I intentionally chose the **Jamendo API** instead.

* **Why:** The Audiomack API requires a complex OAuth 2.0 flow just to fetch a list of tracks. This adds significant complexity (server-side logic, token handling, user logins) that is outside the scope of a fundamentals app.
* **Benefit:** The **Jamendo API** is free, high-quality, and uses a simple API key (`client_id`). This allowed the project to focus 100% on the core requirements: **building a robust MVVM architecture, handling media streaming, and creating a clean UI**.

### 2. Media Player: ExoPlayer (vs. MediaPlayer)

> We initially built the app with Android's `MediaPlayer`, but it was quickly replaced.

* **Assumption:** A modern app must provide a fast, stable streaming experience.
* **Problem:** The built-in `MediaPlayer` is an older API. It suffered from **severe buffering delays (30-60 seconds)** and was unreliable at handling network streams.
* **Solution:** We pivoted to **ExoPlayer (Media3)**. This is the modern standard used by apps like YouTube. It fixed the buffering issue immediately (playback now starts in 1-2 seconds) and provides superior error handling and features (like seeking).

### 3. Network & Error Handling

* **Assumption:** A user might have an unstable or no internet connection.
* **Solution:** The app does not "crash" or show a blank screen.
    * It uses a `NetworkConnectivityService` to check the network state on startup and displays a clear "No internet connection" message.
    * A "Retry" button is provided to give the user control, allowing them to reload the app's data after their connection is restored.
    * A "Buffering..." indicator clearly communicates to the user when a high-quality audio stream is loading.

### 4. Cleartext Traffic

* **Assumption:** The API, while secure itself, might return audio stream URLs that are `http://` instead of `httpss://`.
* **Solution:** To prevent Android from blocking these streams (a default security feature), `android:usesCleartextTraffic="true"` was added to the `AndroidManifest.xml`. In a full production app, I would log this and request that the API provider serve all content over HTTPS.
