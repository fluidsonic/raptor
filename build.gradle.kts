import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.23"
}

fluidLibrary(name = "raptor", version = "0.9.10", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
