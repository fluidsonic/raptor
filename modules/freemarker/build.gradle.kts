import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor"))
				api("org.freemarker:freemarker:2.3.30")

				compileOnly(project(":raptor-kodein"))
			}
		}
	}
}
