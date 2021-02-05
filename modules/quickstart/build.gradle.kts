import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-entities"))
				api(project(":raptor-graphql"))
				api(project(":raptor-ktor"))
				api(project(":raptor-mongodb"))
				api(project(":raptor-security"))
				api(project(":raptor-settings-hocon"))
				api(fluid("country", Versions.fluid_country))
				api(fluid("currency", Versions.fluid_currency))
				api(fluid("stdlib", Versions.fluid_stdlib))
				api(fluid("time", Versions.fluid_time))
			}
		}
	}
}
