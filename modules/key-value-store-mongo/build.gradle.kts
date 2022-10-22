import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-key-value-store"))

				implementation(project(":raptor-mongodb"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
