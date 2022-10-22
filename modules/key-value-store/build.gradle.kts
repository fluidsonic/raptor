import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
