import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))

				implementation(project(":raptor-di"))
				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
