import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-di"))

				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
				implementation("ch.qos.logback:logback-classic:${Versions.logback}")
			}
		}
	}
}
