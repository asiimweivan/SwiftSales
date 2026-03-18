plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.swiftsales"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.swiftsales"
        minSdk = 29
        targetSdk = 36
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
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // Lottie animations
    implementation("com.airbnb.android:lottie:6.4.0")

    // Glide image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ViewPager2 for onboarding
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Dots indicator
    implementation("com.tbuonomo:dotsindicator:5.0")

    // Shimmer effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // ZXing barcode scanner — works offline, no Google Play Services needed
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") { isTransitive = true }
    implementation("com.google.zxing:core:3.5.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}