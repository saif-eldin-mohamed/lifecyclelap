plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "llc.catalystai.lab"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "llc.catalystai.lab"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    // No applicationIdSuffix on debug on purpose: the adb commands in the lesson use
    // llc.catalystai.lab, and a suffix would make them silently do nothing.
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Used directly (withFrameNanos / LaunchedEffect scope). Declared rather than
    // inherited transitively, so a future bump cannot silently remove it.
    implementation(libs.kotlinx.coroutines.android)
}
