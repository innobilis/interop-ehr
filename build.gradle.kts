plugins {
    alias(libs.plugins.interop.gradle.publish) apply false
    alias(libs.plugins.interop.gradle.spring) apply false
    alias(libs.plugins.interop.gradle.version)
    alias(libs.plugins.interop.gradle.junit) apply false
    alias(libs.plugins.interop.version.catalog)
    alias(libs.plugins.interop.gradle.sonarqube)
}

subprojects {
    apply(plugin = "com.projectronin.interop.gradle.publish")
}
