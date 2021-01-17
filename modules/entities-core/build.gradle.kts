import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
				api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
			}
		}

		js(IR)
		jvm()
	}
}
