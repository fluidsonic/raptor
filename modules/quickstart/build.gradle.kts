import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-settings-hocon"))
				api(project(":raptor-graphql"))
				api(project(":raptor-kodein"))
				api(project(":raptor-mongodb"))
				api(fluid("stdlib", "0.10.0"))
				api(fluid("time", "0.10.0"))
			}
		}
	}
}
