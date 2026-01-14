plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "my.little.audio.player.java"
    compileSdk = 36

    defaultConfig {
        applicationId = "my.little.audio.player.java"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib-jdk")) {
            useTarget("org.jetbrains.kotlin:kotlin-stdlib:${requested.version}")
        }
    }
}

dependencies {

    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.appcompat:appcompat:1.6.1")
}