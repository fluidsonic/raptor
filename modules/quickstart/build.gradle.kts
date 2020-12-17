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
//				api(project(":raptor-graphql"))
				api(project(":raptor-ktor"))
				api(project(":raptor-mongodb"))
				api(project(":raptor-settings-hocon"))
				api(fluid("country", "0.9.3"))
				api(fluid("currency", "0.9.3"))
				api(fluid("stdlib", "0.10.3"))
				api(fluid("time", "0.11.0"))
			}
		}
	}
}
