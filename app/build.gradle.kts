plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.magic.music.stream"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ryota.radioku"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val rawPath = project.findProperty("RELEASE_STORE_FILE") as? String
                ?: System.getenv("RELEASE_STORE_FILE")
                ?: "radioku-release.jks"
            val targetFile = if (file(rawPath).exists()) file(rawPath) else file("radioku-release.jks")
            if (targetFile.exists()) {
                storeFile = targetFile
                storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as? String
                    ?: System.getenv("RELEASE_STORE_PASSWORD")
                    ?: "ryota1116"
                keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as? String
                    ?: System.getenv("RELEASE_KEY_ALIAS")
                    ?: "radioku"
                keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as? String
                    ?: System.getenv("RELEASE_KEY_PASSWORD")
                    ?: "ryota1116"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        disable += setOf("InvalidFragmentVersionForActivityResult", "ExpiredTargetSdkVersion")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Media & Media3
    implementation(libs.androidx.media)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Networking (Ktor + JSON)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Image Loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Google Mobile Ads (AdMob) & UMP (User Messaging Platform / GDPR)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
}
