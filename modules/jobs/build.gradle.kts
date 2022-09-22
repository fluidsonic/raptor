import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(fluid("time", Versions.fluid_time))
				api(project(":raptor-core"))
				api(kotlinx("serialization-core", Versions.kotlinx_serialization))
				implementation(project(":raptor-di"))
			}
		}
	}
}
