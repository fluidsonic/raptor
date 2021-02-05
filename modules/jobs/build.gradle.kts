import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(fluid("time", Versions.fluid_time))
			}
		}

		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(kotlinx("serialization-core", Versions.kotlinx_serialization))

				implementation(project(":raptor-di"))
			}
		}
	}
}
