import io.fluidsonic.gradle.*

// TODO Rename module to raptor-transaction?
fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
			}

			testDependencies {
				api(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
