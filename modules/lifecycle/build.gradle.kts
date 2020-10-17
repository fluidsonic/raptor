import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))

				implementation(kotlinx("atomicfu", "0.14.4", usePrefix = false))
				implementation(kotlinx("coroutines-core", "1.3.9"))

				compileOnly(project(":raptor-di"))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", "1.3.9"))
			}
		}
	}
}
