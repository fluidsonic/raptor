import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(fluid("time", "0.13.1"))
			}
		}

		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(kotlinx("serialization-core", "1.0.1"))

				implementation(project(":raptor-di"))
			}
		}
	}
}
