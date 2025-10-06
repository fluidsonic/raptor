import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-key-value-store"))

				implementation(project(":raptor-mongo2"))

				// FIXME rm
				implementation("io.projectreactor:reactor-core:${Versions.reactor}")
				implementation("org.mongodb:mongodb-driver-reactivestreams:${Versions.mongodb}")
				implementation(kotlin("reflect")) // FIXME can we get rid of this?
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(kotlinx("coroutines-reactive", Versions.kotlinx_coroutines))
			}

			testDependencies {
				implementation(kotlinx("coroutines-test", Versions.kotlinx_coroutines))
			}
		}
	}
}
