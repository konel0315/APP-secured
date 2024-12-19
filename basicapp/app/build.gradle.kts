plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.basicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.basicapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.google.android.recaptcha:recaptcha:18.5.1")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.github.iamport:iamport-rest-client-java:0.2.23")
    implementation ("com.google.android.gms:play-services-safetynet:17.0.0")
    implementation ("net.zetetic:android-database-sqlcipher:4.5.3@aar")
}