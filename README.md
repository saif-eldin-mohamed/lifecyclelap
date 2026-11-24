# Lifecycle Lab

The practical for **Module 2, Lesson 2.2 — Processes, Threads, App Lifecycle & Process Death**.

One screen, one file, no networking, no navigation. It exists to make every claim from Lesson 2.1
visible and — more importantly — **falsifiable**. The student does not take your word for it. They
watch the number change.

Deliberately a separate, tiny, Android-only project rather than part of `CatalystFeed`: a six-minute
lesson cannot afford a KMP sync, an iOS framework link, or anything else that might not be warm.

---

## Run it

```bash
./gradlew :app:installDebug
```

Or open the project root in Android Studio and press Run. Needs the Android SDK with API 35.

**No `applicationIdSuffix` on debug, on purpose** — the package is `llc.catalystai.lab` in every
build type, so the `adb` commands below never silently target an app that isn't installed.

---

## What is on the screen

| Element | What it proves |
|---|---|
| **pid** | Process identity. When this number changes, you died. |
| **main thread clock** | Frame-driven, so it freezes exactly when the main thread stops. |
| `remember { }` | Composition-scoped — dies on rotation. |
| `ViewModel` field | Survives rotation. Dies with the process. |
| `object Lab` singleton | Survives rotation. Dies with the process — the myth, killed. |
| `rememberSaveable { }` | Survives rotation **and** a system kill. |
| `SharedPreferences` | Survives until uninstall. |
| **+1 to all five** | Sets up state worth losing, in one tap. |
| **block main thread 3s** | `Thread.sleep` on the main thread. Watch the clock stop. |
| **lifecycle log** | Every callback, with the pid it fired in. `onCreate (fresh)` vs `(RESTORED)`. |

The log lives in the singleton — so it survives the Activity being destroyed and rebuilt, and
vanishes the moment the process dies. That is itself one of the demos.




## Honest notes

- **Versions** are pinned in `gradle/libs.versions.toml`: AGP 8.7.3, Kotlin 2.1.0, Compose UI 1.7.6,
  Material3 1.3.1. A known-compatible set. If sync fails, it is a version in that file.
- **This project has not been compiled.** It was written with no Android SDK and no access to Maven
  Central, so the Kotlin and Gradle wiring were reviewed by hand, not built. Sync it once before you
  record.
- **`Thread.sleep` on the main thread is a bug**, and this app does it on purpose, in one clearly
  named button. If a student copies that pattern into real code, the lesson failed.
- **SharedPreferences is read during composition** on the first frame — technically disk I/O on the
  main thread. Left as-is because it is two values and the alternative would obscure the demo. Worth
  naming out loud if a sharp student catches it.
# lifecyclelap
