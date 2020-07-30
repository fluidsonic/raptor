import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor"))
				api("org.kodein.di:kodein-di-erased-jvm:6.5.5")

				compileOnly(project(":raptor-transactions"))
			}

			testDependencies {
				implementation(project(":raptor-transactions"))
			}
		}
	}
}
