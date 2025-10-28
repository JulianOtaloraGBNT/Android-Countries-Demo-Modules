// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
}

tasks.register<Copy>("prepareFeatureAarsDebug") {
  dependsOn(
    ":features:countries-data-sdk:assembleDebug",
    ":features:countries-ui-artifact:assembleDebug"
  )
  from(layout.projectDirectory.dir("features/countries-data-sdk/build/outputs/aar")) { include("*debug*.aar") }
  from(layout.projectDirectory.dir("features/countries-ui-artifact/build/outputs/aar")) { include("*debug*.aar") }
  into(layout.projectDirectory.dir("libs"))
}

// Removes generated binaries so repo stays clean (new task)
tasks.register<Delete>("cleanGeneratedBinaries") {
  delete(
    // AARs and JARs copied to libs/
    fileTree(layout.projectDirectory.dir("libs")) { include("*.aar", "*.jar") },
    // Feature builds
    layout.projectDirectory.dir("features/countries-data-sdk/build"),
    layout.projectDirectory.dir("features/countries-ui-artifact/build"),
    // App/core module builds (safe to delete; they're recreated)
    layout.projectDirectory.dir("app/build"),
    layout.projectDirectory.dir("core/common/build"),
    layout.projectDirectory.dir("core/domain/build"),
    layout.projectDirectory.dir("core/data/build")
  )
}
