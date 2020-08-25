import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api("org.freemarker:freemarker:2.3.30")

				compileOnly(project(":raptor-di"))
			}
		}
	}
}
