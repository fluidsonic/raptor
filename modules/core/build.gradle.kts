import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
			}
		}

		jvm()
	}
}
