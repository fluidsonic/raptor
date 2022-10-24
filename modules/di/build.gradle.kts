import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
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
