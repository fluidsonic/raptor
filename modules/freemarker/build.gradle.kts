import io.fluidsonic.gradle.*

// TODO Deprecated module. Delete.
fluidLibraryModule(description = "TODO") {
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
