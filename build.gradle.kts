import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.3"
}

fluidLibrary(name = "raptor", version = "0.9.2", prefixName = false) {
	allModules {
		language {
			withoutExplicitApi()
		}
	}
}
