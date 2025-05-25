plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hospital.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hospital.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/hospitalApp.jks")
            storePassword = "ipN2JwzC6WqgNPh3pM3cl3qi6VDfmm3lWxZywsmKqYtAK06CTI"
            keyAlias = "vPuc5vn9HQFKZrd9"
            keyPassword = "8OEVy8IW1EJIMGcAgYRe97xNHJHMjyHdSt2oyMZ72KuQPxCELD"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("io.socket:socket.io-client:2.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.haytham-c01:CurveView:0.1.0")
    implementation("com.github.thomhurst:ExpandableHintText:1.0.7")
}