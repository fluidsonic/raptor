import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-bson"))
				api(fluid("mongo", "1.1.1"))

				implementation(fluid("stdlib", "0.10.0"))
			}
		}
	}
}
