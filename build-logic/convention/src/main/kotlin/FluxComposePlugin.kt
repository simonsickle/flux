import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class FluxComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            try {
                extensions.configure<LibraryExtension> {
                    buildFeatures { compose = true }
                }
            } catch (_: Exception) {
                // App module uses ApplicationExtension
                extensions.configure<com.android.build.gradle.AppExtension> {
                    buildFeatures.compose = true
                }
            }
            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("compose-ui").get())
                add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
                add("implementation", libs.findLibrary("compose-material3").get())
                add("implementation", libs.findLibrary("compose-foundation").get())
                add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
            }
        }
    }
}
