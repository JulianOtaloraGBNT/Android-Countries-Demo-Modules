plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":core:common"))
    
    // Coroutines
    implementation(libs.coroutines.core)

    // Javax Inject for DI annotations
    implementation("javax.inject:javax.inject:1")
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
}
