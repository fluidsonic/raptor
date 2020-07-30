import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor"))

				implementation(kotlinx("atomicfu", "0.14.3-1.4.0-rc", usePrefix = false))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", "1.3.8-1.4.0-rc"))
			}
		}
	}
}
