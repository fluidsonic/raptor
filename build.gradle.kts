import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
	id("io.fluidsonic.gradle") version "4.2.0"
}

fluidLibrary(name = "raptor", version = "0.44.0", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		disableDokka()
	}
}

subprojects {
	plugins.withId("org.jetbrains.kotlin.multiplatform") {
		extensions.configure<KotlinMultiplatformExtension> {
			compilerOptions {
				freeCompilerArgs.add("-Xcontext-sensitive-resolution")
			}
		}
	}
}
