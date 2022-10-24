import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-key-value-store"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
