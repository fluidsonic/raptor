import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.2.2"
}

fluidLibrary(name = "raptor", version = "0.17.21", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
