import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
	id("io.fluidsonic.gradle") version "2.0.2"
}

fluidLibrary(name = "raptor", version = "0.32.0-SNAPSHOT", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		noDokka()
	}
}

subprojects {
	plugins.withId("org.jetbrains.kotlin.multiplatform") {
		extensions.configure<KotlinMultiplatformExtension> {
			compilerOptions {
				freeCompilerArgs.add("-Xcontext-parameters")
				freeCompilerArgs.add("-Xcontext-sensitive-resolution")
			}
		}
	}
}
