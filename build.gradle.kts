import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "2.0.2"
}

fluidLibrary(name = "raptor", version = "0.30.0-SNAPSHOT", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		noDokka()
	}
}
