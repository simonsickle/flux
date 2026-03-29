plugins {
    `kotlin-dsl`
}

group = "dev.simonsickle.flux.buildlogic"

dependencies {
    compileOnly("com.android.tools.build:gradle:8.9.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.10")
    compileOnly("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.10-1.0.29")
    compileOnly("com.google.dagger:hilt-android-gradle-plugin:2.55")
}
