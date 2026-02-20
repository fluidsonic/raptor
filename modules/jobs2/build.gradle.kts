import io.fluidsonic.gradle.*

fluidLibraryModule(description = "RaptorJobs2 - job system with scheduling, execution tracking, and persistent queues") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-domain"))
				api(fluid("time", Versions.fluid_time))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(kotlinx("serialization-core", Versions.kotlinx_serialization))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
				implementation("ch.qos.logback:logback-classic:${Versions.logback}")
			}
		}
	}
}
