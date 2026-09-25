# FixMate — AI Appliance Diagnostics

FixMate is a native Android app that diagnoses home-appliance problems with AI. Snap a photo, describe the symptoms, and get likely faults, safety warnings, and a repair-cost estimate. It's built from scratch in Kotlin + Jetpack Compose and modeled on the FixScan concept — but re-engineered to fix FixScan's key weaknesses (no committed API keys, a current model ID, and a graceful offline mode).

## What it does

The MVP is a three-step flow:

1. **Capture** — take a photo or pick one from the gallery (optional). A clear photo lets the AI spot visible damage, model plates, and error codes.
2. **Describe** — choose the appliance type, brand, and age, then tap the symptoms that apply (burning smell and sparks are flagged as safety-critical).
3. **Diagnose** — the app returns 2–3 likely faults with confidence levels, a safety warning when there's an electrical/fire/water hazard, and an estimated repair-cost range in ₹.

If no Claude API key is configured, FixMate runs in **Demo mode** using a deterministic offline generator, so it's always usable for testing and demos.

## Tech stack

- **Language:** Kotlin 2.0.0
- **UI:** Jetpack Compose (Material 3), single-Activity, Navigation Compose
- **Architecture:** MVVM — `DiagnosisViewModel` (StateFlow) → `DiagnosisRepository` → `ClaudeService`
- **AI:** Anthropic Claude Messages API (`claude-sonnet-4-20250514`) with vision (base64 image) and strict-JSON output
- **Networking:** OkHttp 4.12 + `org.json`
- **Images:** ActivityResult contracts (camera + photo picker), FileProvider, Coil
- **Build:** Android Gradle Plugin 8.5.1, Gradle 8.7, compileSdk/targetSdk 34, minSdk 24, Java 17

## Project structure

```
FixMate/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── local.properties.example         # copy to local.properties, add your key
└── app/
    ├── build.gradle.kts             # reads CLAUDE_API_KEY into BuildConfig
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                     # theme, colors, adaptive icon, file_paths
        └── java/com/fixmate/
            ├── MainActivity.kt
            ├── DiagnosisViewModel.kt
            ├── model/Models.kt
            ├── data/
            │   ├── ClaudeService.kt           # Claude API + vision + JSON parse
            │   ├── MockDiagnosisGenerator.kt  # offline demo fallback
            │   └── DiagnosisRepository.kt
            ├── util/ImageUtils.kt             # capture Uri + base64 downscale
            ├── navigation/NavGraph.kt
            └── ui/
                ├── theme/                     # Color, Type, Theme
                └── screens/                   # Home, Capture, GuidedForm, Results
```

## Setup & run

### 1. Open the project

Open the `FixMate` folder in **Android Studio** (Hedgehog or newer). On first open, Android Studio downloads the Gradle wrapper and syncs automatically.

> **Note:** This project ships without the `gradle/wrapper/` files. If you build from the command line and there's no `gradlew`, generate the wrapper once with a local Gradle install:
> ```
> cd FixMate
> gradle wrapper --gradle-version 8.7
> ```
> Android Studio handles this for you — this step is only needed for headless/CLI builds.

### 2. Add your Claude API key (optional)

FixMate reads the key from `local.properties` (which is git-ignored) or an environment variable — it is **never** hardcoded in source or committed.

Copy the template and add your key:

```
cp local.properties.example local.properties
```

Then edit `local.properties`:

```
CLAUDE_API_KEY=sk-ant-your-key-here
```

Alternatively, export it in your environment before building:

```
export CLAUDE_API_KEY=sk-ant-your-key-here
```

Leave it blank to run in **Demo mode** (offline generator, no network calls). Get a key at https://console.anthropic.com.

### 3. Build and run

Pick an emulator or device (API 24+) and press **Run**, or from the CLI:

```
./gradlew installDebug
```

## Security note (please read)

FixMate calls the Claude API **directly from the device** using a key baked into `BuildConfig` at build time. This is fine for a prototype or internal build, but **it is not safe for a public production release** — a determined user can extract the key from the APK.

For production, route AI calls through a backend you control:

```
App → your server (holds the secret key) → Anthropic API
```

Your server adds authentication, rate-limiting, and keeps the key off the device. `ClaudeService` is deliberately isolated so you can swap its endpoint to your proxy with a one-line change.

This is the main improvement over the original FixScan, which committed a live API key into its `.env.example`. **Never commit real keys.**

## How this differs from FixScan

- **No committed secrets** — the key is read from git-ignored `local.properties`/env, not checked in.
- **Valid, current model ID** — FixScan referenced non-existent model names (e.g. `gemini-3.5-flash`); FixMate uses a real Claude model and documents where to update it.
- **Always works offline** — a deterministic generator backs every call, so the app degrades gracefully instead of failing when the network or key is unavailable.
- **Robust image handling** — photos are downscaled before upload to keep requests small and fast.

## Limitations & disclaimer

FixMate offers **guidance, not a guaranteed fix**. AI diagnoses can be wrong. For any gas, electrical, or water hazard, unplug the appliance and contact a qualified, licensed technician. Cost estimates are rough ranges based on typical Indian repair rates, not quotes.

The launcher icon uses an adaptive vector (API 26+). On API 24–25 a default icon may appear until density-specific PNGs are added (Android Studio's *Image Asset* tool generates these in one click).

If Anthropic releases a newer model, update the `MODEL` constant in `app/src/main/java/com/fixmate/data/ClaudeService.kt`.
