import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Sin org.jetbrains.kotlin.android: AGP 9 trae Kotlin integrado.
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

/** URL del entorno: propiedad de Gradle (-PurlApi=…) o variable de entorno; nunca escrita en el código. */
fun urlDelEntorno(propiedad: String, variable: String): String =
    (findProperty(propiedad) as String?) ?: System.getenv(variable) ?: ""

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

// Versiones transitivas fijas en gradle.lockfile; para actualizarlas: ./gradlew :androidApp:dependencies --write-locks
dependencyLocking {
    lockAllConfigurations()
}

android {
    namespace = "co.trayectago.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "co.trayectago.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "entorno"
    productFlavors {
        create("pruebas") {
            dimension = "entorno"
            applicationIdSuffix = ".pruebas"
            buildConfigField("String", "URL_API", "\"${urlDelEntorno("urlApi", "URL_API_PRUEBAS")}\"")
            buildConfigField("String", "URL_AUTH", "\"${urlDelEntorno("urlAuth", "URL_AUTH_PRUEBAS")}\"")
        }
        create("produccion") {
            dimension = "entorno"
            buildConfigField("String", "URL_API", "\"${urlDelEntorno("urlApi", "URL_API_PRODUCCION")}\"")
            buildConfigField("String", "URL_AUTH", "\"${urlDelEntorno("urlAuth", "URL_AUTH_PRODUCCION")}\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// AppAuth recibe el regreso de Keycloak en <applicationId>:/oauth2redirect; el sufijo del sabor queda incluido.
androidComponents {
    onVariants { variante ->
        variante.manifestPlaceholders.put("appAuthRedirectScheme", variante.applicationId)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.appauth)
}
