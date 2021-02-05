import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.18"
}

allprojects {
	repositories {
		jcenter() // FIXME remove
//		mavenLocal() // FIXME remove
	}
}

fluidLibrary(name = "raptor", version = "0.9.3-kotlin-1.5", prefixName = false) {
	allModules {
		language {
			withExperimentalApi("io.fluidsonic.raptor.RaptorInternalApi")
		}
	}
}
