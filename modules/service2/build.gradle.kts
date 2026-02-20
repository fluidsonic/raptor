import io.fluidsonic.gradle.*

fluidLibraryModule(description = "RaptorService2 - declarative service framework with input sources") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-di"))
				api(project(":raptor-domain"))
				api(project(":raptor-event"))
				api(project(":raptor-jobs2"))
				api(project(":raptor-lifecycle"))
				api(fluid("time", Versions.fluid_time))
				implementation(kotlin("reflect"))
				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(kotlinx("serialization-core", Versions.kotlinx_serialization))
				implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
			}

			testDependencies {
				implementation(project(":raptor-domain-memory"))
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
				implementation("ch.qos.logback:logback-classic:${Versions.logback}")
			}
		}
	}
}
