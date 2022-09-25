import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-entities"))
				api(project(":raptor-graph"))
				api(project(":raptor-ktor"))
				api(project(":raptor-mongodb"))
				api(project(":raptor-security"))
				api(project(":raptor-settings"))
				api(fluid("country", Versions.fluid_country))
				api(fluid("currency", Versions.fluid_currency))
				api(fluid("stdlib", Versions.fluid_stdlib))
				api(fluid("time", Versions.fluid_time))
			}
		}
	}
}
