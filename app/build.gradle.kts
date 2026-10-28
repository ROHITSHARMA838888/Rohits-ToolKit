plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rohit.toolkit"
    compileSdk {
        version = release(35)
    }

    defaultConfig {
        applicationId = "com.rohit.toolkit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("ROHIT_KEYSTORE_PATH") ?: project.findProperty("ROHIT_KEYSTORE_PATH") as String?
            val keystorePass = System.getenv("ROHIT_KEYSTORE_PASSWORD") ?: project.findProperty("ROHIT_KEYSTORE_PASSWORD") as String?
            val keyAlias = System.getenv("ROHIT_KEY_ALIAS") ?: project.findProperty("ROHIT_KEY_ALIAS") as String?
            val keyPass = System.getenv("ROHIT_KEY_PASSWORD") ?: project.findProperty("ROHIT_KEY_PASSWORD") as String?

            if (keystorePath != null && keystorePass != null && keyAlias != null && keyPass != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePass
                this.keyAlias = keyAlias
                this.keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // WorkManager (Background updates)
    implementation(libs.androidx.work.runtime.ktx)

    // Networking & JSON
    implementation(libs.okhttp)
    implementation(libs.gson)

    // Image loading
    implementation(libs.coil.compose)

    // PDF processing
    implementation(libs.pdfbox.android)

    // Google ML Kit
    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.play.services.mlkit.face.detection)
    implementation(libs.mlkit.translate)
    implementation(libs.mlkit.language.id)
    implementation(libs.play.services.mlkit.document.scanner)

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui)
    debugImplementation(libs.androidx.ui.tooling)
}
