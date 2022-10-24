import io.fluidsonic.gradle.*

// TODO Deprecated module. Delete.
fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				api(project(":raptor-dsl"))
				api(kotlinx("serialization-core", Versions.kotlinx_serialization))
			}
		}

		js()
		jvm()
	}
}
