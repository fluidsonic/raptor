import io.fluidsonic.gradle.*

// FIXME rn module to raptor-transaction?
fluidLibraryModule(description = "FIXME") {
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
