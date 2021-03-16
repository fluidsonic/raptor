import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				implementation(project(":raptor-di"))

				api(project(":raptor-core"))
				api("org.freemarker:freemarker:${Versions.freemarker}")
			}
		}
	}
}
