plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.julianotalora.features.countriesdatasdk"
  compileSdk = 35

  defaultConfig {
    minSdk = 24
    consumerProguardFiles("consumer-rules.pro")
    buildConfigField("String", "SDK_BASE_URL", "\"https://restcountries.com\"")
  }

  buildFeatures { buildConfig = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions { jvmTarget = "17" }

  publishing {
    singleVariant("debug") { withSourcesJar() }
  }
}

dependencies {
  // Kotlin / Coroutines
  implementation(libs.coroutines.core)
  implementation(libs.coroutines.android)

  // Serialization
  implementation(libs.kotlinx.serialization.json)

  // Networking
  implementation(libs.retrofit)
  implementation(libs.retrofit.kotlinx.serialization.converter)
  implementation(libs.okhttp)
  implementation(libs.okhttp.logging.interceptor)

  // Room (wired later)
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // Tests (lightweight)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(libs.mockwebserver)
}
