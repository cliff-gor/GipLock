plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.cliffgor.mysmartlock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cliffgor.mysmartlock"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }

        packagingOptions {
            pickFirst("lib/*/libc++_shared.so")
        }
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

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

configurations.all {
    exclude(group = "com.thingclips.smart", module = "thingsmart-modularCampAnno")
    exclude(group = "com.umeng.umsdk", module = "huawei-basetb")
    exclude(group = "commons-io", module = "commons-io")
    exclude(group = "org.apache.commons", module = "commons-lang3")
    exclude(group = "com.thingclips.smart", module = "thingsmart-application-setting")
    // exclude(group = "com.thingclips.smart", module = "react-native")
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.thingclips.smart:thingsmart-expansion-sdk:6.4.0")
    // 设备控制业务包 - 必选
    implementation("com.thingclips.smart:thingsmart-bizbundle-panel")
    // 基础扩展能力 - 必选
    implementation("com.thingclips.smart:thingsmart-bizbundle-basekit")
    // 业务扩展能力 - 必选
    implementation("com.thingclips.smart:thingsmart-bizbundle-bizkit")
    // 设备控制相关能力 - 必选
    implementation("com.thingclips.smart:thingsmart-bizbundle-devicekit")

    implementation("com.thingclips.smart:thingsmart-bizbundle-map_amap")
    implementation("com.thingclips.smart:thingsmart-bizbundle-location_amap")
    implementation("com.thingclips.smart:thingsmart-bizbundle-map_google")
    implementation("com.thingclips.smart:thingsmart-bizbundle-location_google")
    api("com.thingclips.smart:thingsmart-bizbundle-qrcode_mlkit")

    implementation("com.thingclips.smart:sweeper:6.0.3")

    implementation("com.facebook.soloader:soloader:0.10.4+")
    implementation("com.thingclips.smart:thingsmart:6.4.0")

    api(enforcedPlatform("com.thingclips.smart:thingsmart-BizBundlesBom:6.2.12"))

    implementation("com.thingclips.smart:thingsmart-bizbundle-setting:6.2.0")
//    implementation("com.thingclips.smart:thingsmart-application-setting:6.2.0")

    api("com.thingclips.smart:thingsmart-bizbundle-family")
    implementation("com.thingclips.smart:thingsmart-bizbundle-device_activator")

//    Compose Navigation 3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
}