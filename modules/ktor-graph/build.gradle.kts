import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				implementation(fluid("json-basic", Versions.fluid_json))
				implementation(fluid("graphql-execution", Versions.fluid_graphql))

				api(project(":raptor-graph"))
				api(project(":raptor-ktor"))
			}
		}
	}
}
