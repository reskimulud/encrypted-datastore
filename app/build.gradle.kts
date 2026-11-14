plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val cipherKey: String = rootProject.extra["cipherKey"] as String
val iv: String = rootProject.extra["iv"] as String

android {
    namespace = "io.github.reskimulud.encrypteddatastore"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.reskimulud.encrypteddatastore"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CIPHER_KEY", "\"$cipherKey\"")
        buildConfigField("String", "IV", "\"$iv\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    implementation(libs.datastore.typed)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(project(":encrypteddatastore"))
    implementation(libs.circleimageview)
    implementation(libs.glide)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
}
