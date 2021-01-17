import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
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
