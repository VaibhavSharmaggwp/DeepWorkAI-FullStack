import java.util.Properties
import java.net.NetworkInterface
import java.net.Inet4Address

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.deepworkai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.deepworkai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        var backendUrl = localProperties.getProperty("BACKEND_URL")
        if (backendUrl.isNullOrBlank()) {
            var ip: String? = null
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    if (networkInterface.isLoopback || !networkInterface.isUp) continue
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            ip = address.hostAddress
                            break
                        }
                    }
                    if (ip != null) break
                }
            } catch (e: Exception) {}
            backendUrl = if (ip != null) "http://$ip:8080" else "http://10.0.2.2:8080"
            println("Auto-detected local IP for BACKEND_URL: $backendUrl")
        }
        buildConfigField("String", "BACKEND_URL", "\"$backendUrl\"")

        val googleClientId = localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Lottie for Compose
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.22")
    // google Oauth
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // For the icons
    implementation("androidx.compose.material:material-icons-extended")

    val ktor_version = "3.0.3"
    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-android:${ktor_version}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")

    implementation("io.ktor:ktor-client-logging:${ktor_version}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // For Coil (loading the logo if it's a URL, or just use resource)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Fancy Toasts from GitHub
    implementation("com.github.GrenderG:Toasty:1.5.2")
}