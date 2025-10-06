import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))

				implementation(kotlin("reflect")) // FIXME can we get rid of this?
				implementation(fluid("country", Versions.fluid_country))
				implementation(fluid("currency", Versions.fluid_currency))
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(fluid("time", Versions.fluid_time))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(kotlinx("coroutines-reactive", Versions.kotlinx_coroutines))
				implementation("io.projectreactor:reactor-core:${Versions.reactor}")
				implementation("org.mongodb:mongodb-driver-reactivestreams:${Versions.mongodb}")
			}
		}
	}
}
