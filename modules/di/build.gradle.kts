import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				implementation(project(":raptor-transactions"))
				implementation(kotlin("reflect"))

				api(project(":raptor-core"))
			}
		}
	}
}
