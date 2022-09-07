import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
				api(kotlinx("serialization-core", Versions.kotlinx_serialization))
			}
		}

//		js()
		jvm()
	}
}
