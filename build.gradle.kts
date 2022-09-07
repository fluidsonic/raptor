import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.2.1"
}

fluidLibrary(name = "raptor", version = "0.16.0", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
