import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-graphql"))
				api(project(":raptor-mongodb"))
				api(project(":raptor-settings-hocon"))
				api(fluid("country", "0.9.1"))
				api(fluid("currency", "0.9.1"))
				api(fluid("stdlib", "0.10.2"))
				api(fluid("time", "0.10.3"))
			}
		}
	}
}
