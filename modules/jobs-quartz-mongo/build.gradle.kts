import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				implementation(fluid("stdlib", "0.10.3"))
			}
		}

		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-jobs"))
				api(project(":raptor-lifecycle"))

				implementation(kotlinx("atomicfu", "0.14.4", usePrefix = false))
				implementation(kotlinx("coroutines-core", "1.4.2"))
				implementation(kotlinx("serialization-json", "1.0.1"))
				implementation("com.novemberain:quartz-mongodb:2.2.0-rc2")
				implementation("org.mongodb:mongodb-driver-sync:4.1.1")
				implementation("org.quartz-scheduler:quartz:2.3.2")
			}
		}
	}
}
