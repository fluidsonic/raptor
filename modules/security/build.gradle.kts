import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
			}
		}
	}
}
