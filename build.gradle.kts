import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "2.0.1"
}

fluidLibrary(name = "raptor", version = "0.29.0", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		noDokka()
	}
}
