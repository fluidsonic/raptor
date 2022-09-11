import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
			}
		}

		jvm()
	}
}
