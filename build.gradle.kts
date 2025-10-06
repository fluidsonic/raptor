import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "2.0.0"
}

fluidLibrary(name = "raptor", version = "0.27.0", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		noDokka()
	}
}
