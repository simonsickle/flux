import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project

class FluxFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("flux.android.library")
                apply("flux.compose")
                apply("flux.hilt")
            }
            val libs = extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>()
                .named("libs")
            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:model"))
                add("implementation", project(":domain"))
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("navigation-compose").get())
            }
        }
    }
}
