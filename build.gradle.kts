import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.11"
}

allprojects {
	repositories {
		mavenLocal() // FIXME remove
	}
}

fluidLibrary(name = "raptor", version = "0.9.2", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.InternalRaptorApi")
			withoutExplicitApi()
		}
	}
}
