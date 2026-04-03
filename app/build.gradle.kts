plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)          // <--- PHẢI CÓ DÒNG NÀY THÌ LỆNH ksp() Ở DƯỚI MỚI CHẠY ĐƯỢC
    alias(libs.plugins.hilt.android) // <--- PHẢI CÓ DÒNG NÀY THÌ HILT MỚI CHẠY ĐƯỢC
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.thuctaptotnghiep"
    compileSdk = 35 // Sửa lại thành số đơn giản, xóa bỏ đoạn release(36) và minorApiLevel

    defaultConfig {
        applicationId = "com.example.thuctaptotnghiep"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Khối release chỉ nhận cặp ngoặc nhọn {}, không có ngoặc tròn ()
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

    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Các thư viện hệ thống (Giữ nguyên libs.xxx của bạn)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Hilt: Quản lý Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit: Gọi API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Firebase: Auth và Cloud Messaging
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Coil: Hiển thị hình ảnh
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
}