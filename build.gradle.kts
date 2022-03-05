import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.targets.js.nodejs.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.25"
}

fluidLibrary(name = "raptor", version = "0.10.1", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}

// https://youtrack.jetbrains.com/issue/KT-49109
plugins.withType<NodeJsRootPlugin> {
	the<NodeJsRootExtension>().nodeVersion = "16.13.1"
}
