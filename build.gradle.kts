import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.3.2"
}

fluidLibrary(name = "raptor", version = "0.19.0", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
