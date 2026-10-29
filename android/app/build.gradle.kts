plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
}

android {
  namespace = "com.cinetrack"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.cinetrack"
    minSdk = 26
    targetSdk = 34
    versionCode = 1
    versionName = "0.1.0"
    buildConfigField("String", "API_BASE_URL", "\"https://tua-app-su-render.onrender.com/\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui:1.7.0")
  implementation("androidx.compose.material3:material3:1.2.1")
  implementation("androidx.compose.material:material-icons-extended:1.7.0")
  implementation("androidx.navigation:navigation-compose:2.7.7")
  implementation("androidx.datastore:datastore-preferences:1.1.1")
  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("io.coil-kt:coil-compose:2.6.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
