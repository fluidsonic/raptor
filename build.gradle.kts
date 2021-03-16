import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.22"
}

allprojects {
	repositories {
		jcenter() // FIXME remove
	}
}

fluidLibrary(name = "raptor", version = "0.9.9", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
