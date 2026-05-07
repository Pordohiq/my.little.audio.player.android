plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "my.little.audio.player.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "my.little.audio.player.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.media)
    implementation(libs.documentfile)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    coreLibraryDesugaring(libs.android.desugarJdkLibs)
}