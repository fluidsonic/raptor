import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.3.2"
}

fluidLibrary(name = "raptor", version = "0.27.0-SNAPSHOT", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}

		noDokka()
	}
}
