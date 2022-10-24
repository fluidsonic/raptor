import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
			}
		}

		jvm()
	}
}
