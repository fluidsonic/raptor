import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-graphql"))
				api(project(":raptor-mongodb"))
				api(project(":raptor-settings-hocon"))
				api(fluid("stdlib", "0.10.1"))
				api(fluid("time", "0.10.2"))
			}
		}
	}
}
