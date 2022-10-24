import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		common {
			dependencies {
				implementation(fluid("stdlib", Versions.fluid_stdlib))
			}
		}

		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-jobs"))
				api(project(":raptor-lifecycle"))

				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(kotlinx("serialization-json", Versions.kotlinx_serialization))
				implementation("io.fluidsonic.mirror:quartz-mongodb:2.2.0-rc2")
				implementation("org.mongodb:mongodb-driver-sync:${Versions.mongodb}")
				implementation("org.quartz-scheduler:quartz:2.3.2")
			}
		}
	}
}
