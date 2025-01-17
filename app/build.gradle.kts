import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")                                   //for hilt+room
    id("com.google.dagger.hilt.android")                //for hilt
    id("kotlin-parcelize")                              //for Parcelize
    kotlin("plugin.serialization") version "2.0.21"     //for Navigation
    id("androidx.navigation.safeargs.kotlin")           //for Navigation/safeArgs
}

//To hide API_KEY
fun getApiKey(): String {
    val properties = Properties()
    properties.load(rootProject.file("local.properties").inputStream())
    return properties.getProperty("API_KEY")
}


android {
    namespace = "com.akhijix.themule"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.akhijix.themule"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ////To hide API_KEY
        buildConfigField("String", "API_KEY", "\"${getApiKey()}\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true                              //Data Binding enabled
        buildConfig = true                              //To hide API_KEY
    }

}

kapt {                                                  //for hilt
    correctErrorTypes = true
}


dependencies {

    //Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)
    kapt(libs.androidx.room.compiler)
    //Paging3
    implementation(libs.androidx.room.paging)
    //Jetpack Datastore / SharedPrefs
    implementation(libs.androidx.datastore.preferences)

    //Hilt                                              //(+1 change in project.gradle;  +2 changes above //marked )
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    //Coil image handling
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)            // if any errors -> remove the dependency causing the error, try importing all other dependencies, then at the end import the problem dependency.


    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //SavedState Module for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    //Lifecycle only
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)       // contains Flow

    //Retrofit
    implementation(libs.retrofit)
    //Gson
    implementation(libs.gson)
    implementation (libs.converter.gson)

    //WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    androidTestImplementation(libs.androidx.work.testing)
    implementation(libs.androidx.work.multiprocess)

    //Navigation                                        //+1 safeArgs in project.gradle; +2 in plugins above
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.dynamic.features.fragment)
    implementation(libs.kotlinx.serialization.json)
    androidTestImplementation(libs.androidx.navigation.testing)

    //RecyclerView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.recyclerview.selection)

    //CardView
    implementation(libs.androidx.cardview)

    //ConstraintLayout
    implementation(libs.androidx.constraintlayout)

    //Fragments
    implementation(libs.androidx.fragment.ktx)
    debugImplementation(libs.androidx.fragment.testing)

    //SwipeRefreshLayout
    implementation (libs.androidx.swiperefreshlayout)

}

