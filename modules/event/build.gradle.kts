import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				implementation(project(":raptor-di"))
				implementation(project(":raptor-lifecycle"))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}

		jvm()
	}
}
